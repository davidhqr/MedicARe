package dev.medicare;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.PixelCopy;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.TransformableNode;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.medicare.models.Annotation;
import dev.medicare.models.Dosage;
import dev.medicare.models.Feature;
import dev.medicare.models.GoogleRequest;
import dev.medicare.models.GoogleResponse;
import dev.medicare.models.GoogleService;
import dev.medicare.models.Image;
import dev.medicare.models.ImageRequest;
import dev.medicare.models.LogoAnnotation;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ArFragment extends Fragment implements View.OnClickListener {

    private FragmentActivity mainActivity;
    private View mainView;
    private ViewPager viewPager;

    TinyDB tinyDB;

    Session session;
    boolean sessionConfigured = false;

    com.google.ar.sceneform.ux.ArFragment arFragment;
    AnchorNode anchorNode;
    TransformableNode node;

    Vision vision;

    View mainLayout;
    ImageView mainCrosshair;
    CircularProgressBar mainProgress;
    Button mainButton;

    TextView labelTitle;
    TextView labelDescription;
    TextView labelDosage;
    Button labelTakePillButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainActivity = getActivity();
        mainView = inflater.inflate(R.layout.fragment_ar, container, false);

        tinyDB = new TinyDB(mainActivity);
        if (tinyDB.getListObject("history", Dosage.class) == null) {
            tinyDB.putListObject("history", new ArrayList<>());
        }

        Vision.Builder visionBuilder = new Vision.Builder(
                new NetHttpTransport(),
                new AndroidJsonFactory(),
                null);
        visionBuilder.setVisionRequestInitializer(
                new VisionRequestInitializer("AIzaSyABb8C6veM8c9cU3ad3Xrm6JurRKAZL19s"));

        vision = visionBuilder.build();

        initViews();
        return mainView;
    }

    private void initViews() {
        arFragment = (com.google.ar.sceneform.ux.ArFragment) getChildFragmentManager().findFragmentById(R.id.main_ar_fragment);
        arFragment.getTransformationSystem().setSelectionVisualizer(new CustomVisualizer());
        arFragment.getArSceneView().getPlaneRenderer().setEnabled(false);

        viewPager = mainActivity.findViewById(R.id.main_viewPager);
        mainLayout = mainView.findViewById(R.id.main_layout);
        mainCrosshair = mainView.findViewById(R.id.main_crosshair);
        mainProgress = mainView.findViewById(R.id.main_progress);
        mainButton = mainView.findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_button:
                if (mainButton.getText().equals("SCAN")) {
                    hideButton();
                    takePhoto();
                } else {
                    anchorNode.removeChild(node);
                    resetView();
                }
                break;
            case R.id.label_take_pill_button:
                Toast.makeText(mainActivity, "Medicine added to history", Toast.LENGTH_SHORT).show();
                viewPager.setCurrentItem(2);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (session != null) {
            arFragment.getArSceneView().pause();
            session.pause();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (session == null) {
            try {
                session = new Session(mainActivity);
            } catch (Exception e) {
                e.printStackTrace();
            }
            sessionConfigured = true;
        }

        if (sessionConfigured) {
            configureSession();
            sessionConfigured = false;

            arFragment.getArSceneView().setupSession(session);
        }
    }

    private void takePhoto() {
        ArSceneView view = arFragment.getArSceneView();
        final Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        final HandlerThread handlerThread = new HandlerThread("PixelCopier");
        handlerThread.start();

        PixelCopy.request(view, bitmap, (copyResult) -> {
            handlerThread.quitSafely();

            if (copyResult == PixelCopy.SUCCESS) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] data = stream.toByteArray();
                bitmap.recycle();

                processImage(data);

            } else {
                Toast.makeText(mainActivity, "Failed to save image", Toast.LENGTH_LONG).show();
            }
        }, new Handler(handlerThread.getLooper()));
    }

    private void processImage(byte[] imageData) {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://vision.googleapis.com/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        GoogleService googleService = retrofit.create(GoogleService.class);

        Image image = new Image(Base64.getEncoder().encodeToString(imageData));
        List<Feature> features = new ArrayList<>();
        features.add(new Feature("LOGO_DETECTION"));
        List<ImageRequest> imageRequests = Collections.singletonList(new ImageRequest(image, features));
        GoogleRequest googleRequest = new GoogleRequest(imageRequests);

        try {
            GoogleResponse googleResponse = googleService.getResponse("", googleRequest).execute().body();
            if (googleResponse == null) {
                Toast.makeText(mainActivity, "No medicine found", Toast.LENGTH_SHORT).show();
                resetView();
                return;
            }

            List<Annotation> annotations = googleResponse.getAnnotations();
            if (annotations == null || annotations.isEmpty()) {
                Toast.makeText(mainActivity, "No medicine found", Toast.LENGTH_SHORT).show();
                resetView();
                return;
            }

            List<LogoAnnotation> logoAnnotations = annotations.get(0).getLogoAnnotations();
            if (logoAnnotations == null || logoAnnotations.isEmpty()) {
                Toast.makeText(mainActivity, "No medicine found", Toast.LENGTH_SHORT).show();
                resetView();
                return;
            }

            LogoAnnotation logoAnnotation = logoAnnotations.get(0);
            mainActivity.runOnUiThread(() -> showAR(logoAnnotation.getDescription()));

        } catch (Exception ex) {
            Toast.makeText(mainActivity, "Error connecting to Google Cloud Vision", Toast.LENGTH_SHORT).show();
            resetView();
        }
    }

    private void showAR(String brand) {
        List<HitResult> hitResults = arFragment.getArSceneView().getArFrame().hitTest(getScreenCenter().x, getScreenCenter().y);

        if (hitResults == null || hitResults.isEmpty()) {
            Toast.makeText(mainActivity, "No surface detected", Toast.LENGTH_LONG).show();
            resetView();
            return;
        }

        HitResult hitResult = hitResults.get(0);
        Anchor anchor = session.createAnchor(hitResult.getHitPose().compose(Pose.makeTranslation(0, 0.0f, 0)));

        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View label = inflater.inflate(R.layout.label_view, null);
        setUpLabel(label, brand);

        renderObject(arFragment, anchor, label);
        showBackButton();
    }

    private void setUpLabel(View label, String brand) {
        labelTitle = label.findViewById(R.id.label_title);
        labelDescription = label.findViewById(R.id.label_description);
        labelDosage = label.findViewById(R.id.label_dosage);
        labelTakePillButton = label.findViewById(R.id.label_take_pill_button);
        labelTakePillButton.setOnClickListener(this);

        ArrayList<Object> history = tinyDB.getListObject("history", Dosage.class);

        if (brand.equalsIgnoreCase("paracetamol") || brand.equalsIgnoreCase("tylenol")) {
            history.add(new Dosage("Tylenol", 1, 500));
            labelTitle.setText(R.string.label_title_tylenol);
            labelDescription.setText(R.string.label_description_tylenol);
            labelDosage.setText(getString(R.string.label_dosage, numMedsToday("Tylenol", history), 5));

        } else if (brand.equalsIgnoreCase("advil")) {
            history.add(new Dosage("Advil", 1, 500));
            labelTitle.setText(R.string.label_title_advil);
            labelDescription.setText(R.string.label_description_advil);
            labelDosage.setText(getString(R.string.label_dosage, numMedsToday("Advil", history), 5));

        }

        tinyDB.putListObject("history", history);
    }

    private void renderObject(com.google.ar.sceneform.ux.ArFragment fragment, Anchor anchor, View label) {
        ViewRenderable.builder()
                .setView(mainActivity, label)
                .setSizer(view -> new Vector3(0.1f, 0.068f, 0.1f))
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally((throwable -> {
                    System.out.println(throwable.getMessage());
                    return null;
                }));
    }

    private void addNodeToScene(com.google.ar.sceneform.ux.ArFragment fragment, Anchor anchor, Renderable renderable) {
        anchorNode = new AnchorNode(anchor);
        node = new TransformableNode(fragment.getTransformationSystem());

        node.setRenderable(renderable);
        node.setParent(anchorNode);

        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

    private Point getScreenCenter() {
        if (arFragment == null || arFragment.getView() == null) {
            return new android.graphics.Point(0, 0);
        }

        int w = arFragment.getView().getWidth() / 2;
        int h = arFragment.getView().getHeight() / 2;

        return new android.graphics.Point(w, h);
    }

    private void configureSession() {
        Config config = new Config(session);

        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setFocusMode(Config.FocusMode.AUTO);

        session.configure(config);
    }

    private void resetView() {
        mainActivity.runOnUiThread(() -> {
            mainProgress.setVisibility(View.INVISIBLE);
            mainCrosshair.setVisibility(View.VISIBLE);
            mainButton.setVisibility(View.VISIBLE);
            mainButton.setBackground(mainActivity.getDrawable(R.drawable.btn_rounded_scan));
            mainButton.setText(R.string.scan_buton);
        });
    }

    private void showBackButton() {
        mainActivity.runOnUiThread(() -> {
            mainProgress.setVisibility(View.INVISIBLE);
            mainButton.setVisibility(View.VISIBLE);
            mainButton.setBackground(mainActivity.getDrawable(R.drawable.btn_rounded_back));
            mainButton.setText(R.string.back_button);
        });
    }

    private void hideButton() {
        mainActivity.runOnUiThread(() -> {
            mainProgress.setVisibility(View.VISIBLE);
            mainCrosshair.setVisibility(View.INVISIBLE);
            mainButton.setVisibility(View.INVISIBLE);
        });
    }

    private int numMedsToday(String med, ArrayList<Object> history) {
        int count = 0;
        for (Object o : history) {
            Dosage dosage = (Dosage) o;
            long diff = (new Date()).getTime() - dosage.getTimestamp().getTime();
            long diffMins = TimeUnit.MINUTES.convert(diff, TimeUnit.MILLISECONDS);
            if (dosage.getName().equalsIgnoreCase(med) && diffMins < 1440) {
                ++count;
            }
        }
        return count;
    }
}
