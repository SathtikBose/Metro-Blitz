# Proguard rules for Metro Blitz (REL-02: R8 does not break model loading, DataStore or SceneView)

# Google Filament & SceneView native rendering and JNI bindings
-keep class com.google.android.filament.** { *; }
-keep interface com.google.android.filament.** { *; }
-keep class io.github.sceneview.** { *; }
-keep interface io.github.sceneview.** { *; }
-keep class dev.romainguy.kotlin.math.** { *; }

# AndroidX DataStore Preferences serialization
-keep class androidx.datastore.preferences.** { *; }

# Preserve Metro Blitz models and catalog
-keep class com.buildstack.metroblitz.core.** { *; }
-keep class com.buildstack.metroblitz.render.AssetCatalog { *; }
