package com.rituraj.awgment;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.ar.core.Anchor;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;
import com.rituraj.awgment.R;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // object of ArFragment Class
    private ArFragment arCam;

    // helps to render the 3d model
    // only once when we tap the screen
    private int clickNo = 0;

    public static boolean checkSystemSupport(Activity activity) {

        // checking whether the API version of the running Android >= 24
        // that means Android Nougat 7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).getDeviceConfigurationInfo().getGlEsVersion();

            // checking whether the OpenGL version >= 3.0
            if (Double.parseDouble(openGlVersion) >= 3.0) {
                return true;
            } else {
                Toast.makeText(activity, "App needs OpenGl Version 3.0 or later", Toast.LENGTH_SHORT).show();
                activity.finish();
                return false;
            }
        } else {
            Toast.makeText(activity, "App does not support required Build Version", Toast.LENGTH_SHORT).show();
            activity.finish();
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSystemSupport(this)) {

            // ArFragment is linked up with its respective id used in the activity_main.xml
            arCam = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.arCameraArea);
            arCam.setOnTapArPlaneListener((hitResult, plane, motionEvent) -> {
                clickNo++;
                // the 3d model comes to the scene only
                // when clickNo is one that means once
                if (clickNo == 1) {
                    Anchor anchor = hitResult.createAnchor();
                    ModelRenderable.builder()
                            .setSource(this, R.raw.model)
                            .setIsFilamentGltf(true)
                            .build()
                            .thenAccept(modelRenderable -> addModel(anchor, modelRenderable))
                            .exceptionally(throwable -> {
                                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                builder.setMessage("Somthing is not right" + throwable.getMessage()).show();
                                return null;
                            });
                }
            });
        }
    }

    private void addModel(Anchor anchor, ModelRenderable modelRenderable) {

        // Creating a AnchorNode with a specific anchor
        AnchorNode anchorNode = new AnchorNode(anchor);

        // attaching the anchorNode with the ArFragment
        anchorNode.setParent(arCam.getArSceneView().getScene());

        // attaching the anchorNode with the TransformableNode
        TransformableNode model = new TransformableNode(arCam.getTransformationSystem());
        model.setParent(anchorNode);

        // attaching the 3d model with the TransformableNode
        // that is already attached with the node
        model.setRenderable(modelRenderable);
        model.select();
    }
}
