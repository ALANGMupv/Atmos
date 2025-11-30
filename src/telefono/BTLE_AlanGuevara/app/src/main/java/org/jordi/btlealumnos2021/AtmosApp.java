package org.jordi.btlealumnos2021;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Controla si la app pasa de background → foreground
 * para decidir si mostrar la autenticación biométrica.
 */
public class AtmosApp extends Application {

    private int actividadesEnForeground = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityStarted(Activity activity) {

                // Si no había actividades en foreground,
                // esto significa que la app vuelve desde background.
                if (actividadesEnForeground == 0) {
                    AppLifecycleTracker.appFueCerrada = false;
                }

                actividadesEnForeground++;
            }

            @Override
            public void onActivityStopped(Activity activity) {
                actividadesEnForeground--;

                // Si ninguna actividad está visible,
                // la app se va al background → se considera "cerrada".
                if (actividadesEnForeground == 0) {
                    AppLifecycleTracker.appFueCerrada = true;
                }
            }

            // Métodos que no necesitas implementar
            @Override public void onActivityCreated(Activity a, Bundle b) {}
            @Override public void onActivityResumed(Activity a) {}
            @Override public void onActivityPaused(Activity a) {}
            @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
            @Override public void onActivityDestroyed(Activity a) {}
        });
    }
}
