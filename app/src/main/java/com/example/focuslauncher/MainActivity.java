package com.example.focuslauncher;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class MainActivity extends Activity {
private HorizontalScrollView screenPager;
private LinearLayout pagesContainer;
private LinearLayout controlCenterView;
private LinearLayout homeView;
private LinearLayout appDrawerView;
private LinearLayout focusButtonContainer;
private GridLayout appsGrid;
private LinearLayout drawerAppListContainer;
private EditText searchField;
private SharedPreferences prefs;
private PackageManager packageManager;
private String currentFocusMode;
private Set focusModesList;
private List masterAppList = new ArrayList<>();
@Override
protected void onCreate(Bundle savedInstanceState) {
super.onCreate(savedInstanceState);
prefs = getSharedPreferences("FocusLauncherSettings", MODE_PRIVATE);
packageManager = getPackageManager();
screenPager = new HorizontalScrollView(this);
screenPager.setHorizontalScrollBarEnabled(false);
screenPager.setSmoothScrollingEnabled(true);
pagesContainer = new LinearLayout(this);
pagesContainer.setOrientation(LinearLayout.HORIZONTAL);
screenPager.addView(pagesContainer);
int displayWidth = getResources().getDisplayMetrics().widthPixels;
LinearLayout.LayoutParams pageStructure = new LinearLayout.LayoutParams(
displayWidth, ViewGroup.LayoutParams.MATCH_PARENT);
controlCenterView = new LinearLayout(this);
controlCenterView.setOrientation(LinearLayout.VERTICAL);
controlCenterView.setPadding(64, 80, 64, 64);
controlCenterView.setLayoutParams(pageStructure);
buildControlCenter();
ScrollView homeScroll = new ScrollView(this);
homeScroll.setLayoutParams(pageStructure);
homeScroll.setFillViewport(true);
homeView = new LinearLayout(this);
homeView.setOrientation(LinearLayout.VERTICAL); homeView.setPadding(40, 80, 40, 64);
homeScroll.addView(homeView);
buildHomeScreen();
appDrawerView = new LinearLayout(this);
appDrawerView.setOrientation(LinearLayout.VERTICAL);
appDrawerView.setPadding(40, 80, 40, 40);
appDrawerView.setLayoutParams(pageStructure);
buildAppDrawer();
pagesContainer.addView(controlCenterView);
pagesContainer.addView(homeScroll);
pagesContainer.addView(appDrawerView);
focusModesList = prefs.getStringSet("focus_modes", null);
if (focusModesList == null) {
focusModesList = new HashSet<>();
focusModesList.add("PERSONAL");
focusModesList.add("SLEEP");
focusModesList.add("WORK");
prefs.edit().putStringSet("focus_modes", focusModesList).apply();
}
currentFocusMode = prefs.getString("active_focus_mode", "PERSONAL");
cacheSystemApplications();
applyFocusProfile(currentFocusMode);
setContentView(screenPager);
screenPager.post(() -> screenPager.scrollTo(displayWidth, 0));
}
private void cacheSystemApplications() {
Intent intent = new Intent(Intent.ACTION_MAIN, null);
intent.addCategory(Intent.CATEGORY_LAUNCHER);
masterAppList = packageManager.queryIntentActivities(intent, 0);
}
private void buildControlCenter() {
controlCenterView.setBackgroundColor(Color.parseColor("#1C1C1E"));
TextView header = new TextView(this);
header.setText("Control Center");
header.setTextSize(28);
header.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
header.setTextColor(Color.WHITE);
header.setPadding(0, 0, 0, 64);
controlCenterView.addView(header);
controlCenterView.addView(createStyledMenuButton("■ Wi-Fi Network"));
controlCenterView.addView(createStyledMenuButton("■ Bluetooth Devices"));
}
private View createStyledMenuButton(String title) {
Button btn = new Button(this);
btn.setText(title);
btn.setTextColor(Color.WHITE);
btn.setTextSize(15);
btn.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
btn.setPadding(48, 40, 48, 40);
LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
params.setMargins(0, 0, 0, 24);
btn.setLayoutParams(params);
GradientDrawable shape = new GradientDrawable();
shape.setCornerRadius(32);
shape.setColor(Color.parseColor("#2C2C2E"));
btn.setBackground(shape);
if (title.contains("Wi-Fi")) {
btn.setOnClickListener(v -> startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)));
} else {
btn.setOnClickListener(v -> startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)));
}
return btn;
}
private void buildHomeScreen() {
ImageView backgroundVisualAnchor = new ImageView(this);
LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, 260);
imgParams.setMargins(0, 0, 0, 40);
backgroundVisualAnchor.setLayoutParams(imgParams);
backgroundVisualAnchor.setScaleType(ImageView.ScaleType.CENTER_CROP); backgroundVisualAnchor.setImageResource(android.R.drawable.ic_menu_gallery);
homeView.addView(backgroundVisualAnchor);
focusButtonContainer = new LinearLayout(this);
focusButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
focusButtonContainer.setPadding(0, 16, 0, 32);
homeView.addView(focusButtonContainer);
Button btnCreateFocus = new Button(this);
btnCreateFocus.setText("■ New Focus Profile");
btnCreateFocus.setTextColor(Color.WHITE);
btnCreateFocus.setTextSize(14);
LinearLayout.LayoutParams createParams = new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
createParams.setMargins(0, 0, 0, 64);
btnCreateFocus.setLayoutParams(createParams);
GradientDrawable createShape = new GradientDrawable();
createShape.setCornerRadius(32);
createShape.setColor(Color.parseColor("#3A3A3C"));
btnCreateFocus.setBackground(createShape);
btnCreateFocus.setOnClickListener(v -> showCreateFocusDialog());
homeView.addView(btnCreateFocus);
appsGrid = new GridLayout(this);
appsGrid.setAlignmentMode(GridLayout.ALIGN_BOUNDS);
LinearLayout.LayoutParams gridParams = new LinearLayout.LayoutParams(
ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
appsGrid.setLayoutParams(gridParams);
homeView.addView(appsGrid);
}
private void buildAppDrawer() {
appDrawerView.setBackgroundColor(Color.parseColor("#121212"));
searchField = new EditText(this);
searchField.setHint("Search Applications...");
searchField.setHintTextColor(Color.parseColor("#8E8E93"));
searchField.setTextColor(Color.WHITE);
searchField.setPadding(32, 32, 32, 32);
GradientDrawable searchBoxShape = new GradientDrawable();
searchBoxShape.setCornerRadius(24);
searchBoxShape.setColor(Color.parseColor("#1C1C1E"));
searchField.setBackground(searchBoxShape);
appDrawerView.addView(searchField);
ScrollView scrollDrawer = new ScrollView(this);
scrollDrawer.setPadding(0, 32, 0, 0);
drawerAppListContainer = new LinearLayout(this);
drawerAppListContainer.setOrientation(LinearLayout.VERTICAL);
scrollDrawer.addView(drawerAppListContainer);
appDrawerView.addView(scrollDrawer);
searchField.addTextChangedListener(new TextWatcher() {
@Override
public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
@Override
public void onTextChanged(CharSequence s, int start, int before, int count) {
renderAppDrawerItems(s.toString());
}
@Override
public void afterTextChanged(Editable s) {}
});
}
private void renderAppDrawerItems(String filterText) {
drawerAppListContainer.removeAllViews();
String searchTarget = filterText.toLowerCase().trim();
for (ResolveInfo ri : masterAppList) {
String appLabel = ri.loadLabel(packageManager).toString();
String pkgName = ri.activityInfo.packageName;
if (!searchTarget.isEmpty() && !appLabel.toLowerCase().contains(searchTarget)) {
continue;
}
LinearLayout itemRow = new LinearLayout(this);
itemRow.setOrientation(LinearLayout.HORIZONTAL);
itemRow.setGravity(Gravity.CENTER_VERTICAL);
itemRow.setPadding(24, 28, 24, 28);
ImageView iconView = new ImageView(this);
iconView.setLayoutParams(new LinearLayout.LayoutParams(100, 100)); iconView.setImageDrawable(ri.loadIcon(packageManager));
TextView labelView = new TextView(this);
labelView.setText(appLabel);
labelView.setTextColor(Color.WHITE);
labelView.setTextSize(16);
labelView.setPadding(40, 0, 0, 0);
itemRow.addView(iconView);
itemRow.addView(labelView);
itemRow.setOnClickListener(v -> {
Intent launchIntent = packageManager.getLaunchIntentForPackage(pkgName);
if (launchIntent != null) startActivity(launchIntent);
});
drawerAppListContainer.addView(itemRow);
}
}
private void applyFocusProfile(String mode) {
String defaultBgColor = "#E5F1F4";
int defaultTextSize = 12;
int defaultGridCols = 4;
int defaultTextCol = Color.BLACK;
if ("SLEEP".equals(mode)) {
defaultBgColor = "#121212";
defaultTextCol = Color.WHITE;
defaultGridCols = 3;
} else if ("WORK".equals(mode)) {
defaultBgColor = "#EBF3F9";
defaultGridCols = 5;
}
String activeBg = prefs.getString("focus_bg_" + mode, defaultBgColor);
int activeCols = prefs.getInt("focus_cols_" + mode, defaultGridCols);
int activeTxtCol = prefs.getInt("focus_tcol_" + mode, defaultTextCol);
int activeTxtSz = prefs.getInt("focus_tsz_" + mode, defaultTextSize);
try {
homeView.setBackgroundColor(Color.parseColor(activeBg));
} catch (Exception e) {
homeView.setBackgroundColor(Color.parseColor(defaultBgColor));
}
appsGrid.setColumnCount(activeCols);
renderHomeScreenApps(mode, activeCols, activeTxtCol, activeTxtSz);
renderAppDrawerItems("");
refreshFocusButtons();
}
private void renderHomeScreenApps(final String focusFilter, int cols, int textCol, int textSize) {
appsGrid.removeAllViews();
int displayWidth = getResources().getDisplayMetrics().widthPixels;
int itemWidth = (displayWidth - 80) / cols;
for (ResolveInfo ri : masterAppList) {
final String pkgName = ri.activityInfo.packageName;
final String appLabel = ri.loadLabel(packageManager).toString();
if (prefs.getBoolean("hide_" + focusFilter + "_" + pkgName, false) ||
("WORK".equals(focusFilter) && (pkgName.contains("instagram") || pkgName.contains("tiktok") || pkgName.contai
ns("facebook")))) {
continue;
}
LinearLayout appItem = new LinearLayout(this);
appItem.setOrientation(LinearLayout.VERTICAL);
appItem.setGravity(Gravity.CENTER);
appItem.setPadding(8, 24, 8, 24);
appItem.setLayoutParams(new LinearLayout.LayoutParams(itemWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
ImageView appIcon = new ImageView(this);
int iconScale = prefs.getInt("icon_scale_" + focusFilter, 128);
appIcon.setLayoutParams(new LinearLayout.LayoutParams(iconScale, iconScale));
String customIconColor = prefs.getString("icon_color_" + pkgName, "");
if (!customIconColor.isEmpty()) {
GradientDrawable shape = new GradientDrawable();
shape.setCornerRadius(32);
shape.setColor(Color.parseColor(customIconColor));
appIcon.setBackground(shape);
} else {
appIcon.setImageDrawable(ri.loadIcon(packageManager));
} appItem.setOnLongClickListener(new View.OnLongClickListener() {
@Override
public boolean onLongClick(View v) {
openThemeCustomizerDialog(focusFilter, pkgName, appLabel);
return true;
}
});
TextView appTitle = new TextView(this);
appTitle.setText(appLabel);
appTitle.setTextSize(textSize);
appTitle.setTextColor(textCol);
appTitle.setGravity(Gravity.CENTER);
appTitle.setMaxLines(1);
appTitle.setPadding(0, 12, 0, 0);
appItem.addView(appIcon);
appItem.addView(appTitle);
appItem.setOnClickListener(v -> {
Intent launchIntent = packageManager.getLaunchIntentForPackage(pkgName);
if (launchIntent != null) startActivity(launchIntent);
});
appsGrid.addView(appItem);
}
}
private void refreshFocusButtons() {
focusButtonContainer.removeAllViews();
for (final String mode : focusModesList) {
Button btn = new Button(this);
btn.setText(mode);
btn.setTextSize(11);
btn.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
params.setMargins(8, 0, 8, 0);
btn.setLayoutParams(params);
GradientDrawable shape = new GradientDrawable();
shape.setCornerRadius(40);
if (mode.equals(currentFocusMode)) {
btn.setTextColor(Color.WHITE);
shape.setColor(Color.parseColor("#007AFF"));
} else {
btn.setTextColor(Color.parseColor("#8E8E93"));
shape.setColor(Color.parseColor("#E5E5EA"));
if ("SLEEP".equals(currentFocusMode)) {
shape.setColor(Color.parseColor("#2C2C2E"));
}
}
btn.setBackground(shape);
btn.setPadding(0, 24, 0, 24);
btn.setOnClickListener(v -> switchFocusMode(mode));
focusButtonContainer.addView(btn);
}
}
private void switchFocusMode(String targetMode) {
currentFocusMode = targetMode;
prefs.edit().putString("active_focus_mode", targetMode).apply();
applyFocusProfile(targetMode);
}
private void openThemeCustomizerDialog(final String mode, final String packageName, String appName) {
AlertDialog.Builder builder = new AlertDialog.Builder(this);
builder.setTitle("Theme Parameters: " + mode);
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(40, 24, 40, 24);
final EditText bgInput = new EditText(this);
bgInput.setHint("Theme Background Hex (e.g., #223344)");
layout.addView(bgInput);
final EditText gridInput = new EditText(this);
gridInput.setHint("Grid Layout Columns (e.g., 2, 4, 6)");
layout.addView(gridInput); final EditText scaleInput = new EditText(this);
scaleInput.setHint("Icon Scale Dimensions (Default 128)");
layout.addView(scaleInput);
builder.setView(layout);
builder.setPositiveButton("Repaint Theme", (dialog, which) -> {
String bg = bgInput.getText().toString().trim();
String cols = gridInput.getText().toString().trim();
String scale = scaleInput.getText().toString().trim();
if (!bg.isEmpty()) prefs.edit().putString("focus_bg_" + mode, bg).apply();
if (!cols.isEmpty()) prefs.edit().putInt("focus_cols_" + mode, Integer.parseInt(cols)).apply();
if (!scale.isEmpty()) prefs.edit().putInt("focus_scale_" + mode, Integer.parseInt(scale)).apply();
applyFocusProfile(mode);
});
builder.setNeutralButton("Hide App in " + mode, (dialog, which) -> {
prefs.edit().putBoolean("hide_" + mode + "_" + packageName, true).apply();
applyFocusProfile(mode);
});
builder.setNegativeButton("Reset Layout", (dialog, which) -> {
prefs.edit().remove("focus_bg_" + mode).remove("focus_cols_" + mode).remove("focus_scale_" + mode).apply();
applyFocusProfile(mode);
});
builder.show();
}
private void showCreateFocusDialog() {
AlertDialog.Builder builder = new AlertDialog.Builder(this);
builder.setTitle("Create Custom Focus Profile");
LinearLayout layout = new LinearLayout(this);
layout.setOrientation(LinearLayout.VERTICAL);
layout.setPadding(40, 24, 40, 24);
final EditText inputName = new EditText(this);
inputName.setHint("Focus Name (e.g., GAMING)");
layout.addView(inputName);
builder.setView(layout);
builder.setPositiveButton("Save Profile", (dialog, which) -> {
String name = inputName.getText().toString().trim().toUpperCase();
if (!name.isEmpty()) {
focusModesList.add(name);
prefs.edit().putStringSet("focus_modes", focusModesList).apply();
switchFocusMode(name);
}
});
builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
builder.show();
}
private int pxToDp(int px) {
return (int) (px / getResources().getDisplayMetrics().density);
}
@Override
public void onBackPressed() {
int displayWidth = getResources().getDisplayMetrics().widthPixels;
screenPager.smoothScrollTo(displayWidth, 0);
}
}
