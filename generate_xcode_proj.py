import os
import uuid

# Helper to generate deterministic 24-character hex PBX IDs
def gen_id(name):
    u = uuid.uuid5(uuid.NAMESPACE_DNS, f"wakeout.ios.{name}")
    return u.hex[:24].upper()

proj_dir = r"d:\Wakeout\ios\Wakeout.xcodeproj"
shared_schemes_dir = os.path.join(proj_dir, "xcshareddata", "xcschemes")
workspace_dir = os.path.join(proj_dir, "project.xcworkspace")
os.makedirs(shared_schemes_dir, exist_ok=True)
os.makedirs(workspace_dir, exist_ok=True)

# 1. Generate project.xcworkspace/contents.xcworkspacedata
with open(os.path.join(workspace_dir, "contents.xcworkspacedata"), "w", encoding="utf-8") as f:
    f.write('''<?xml version="1.0" encoding="UTF-8"?>
<Workspace
   version = "1.0">
   <FileRef
      location = "self:">
   </FileRef>
</Workspace>
''')

# Define source and resource files
sources = [
    ("MoveDeskApp.swift", "App/MoveDeskApp.swift", "App"),
    ("AppState.swift", "Core/AppState.swift", "Core"),
    ("Theme.swift", "Core/Theme.swift", "Core"),
    ("MotionDetector.swift", "Core/Camera/MotionDetector.swift", "Camera"),
    ("CameraPreviewView.swift", "Core/Camera/CameraPreviewView.swift", "Camera"),
    ("GestureEngine.swift", "Core/Motion/GestureEngine.swift", "Motion"),
    ("Joint.swift", "Core/Motion/Joint.swift", "Motion"),
    ("OneEuroFilter.swift", "Core/Motion/OneEuroFilter.swift", "Motion"),
    ("GameView.swift", "Features/Games/GameView.swift", "Games"),
    ("ChallengeView.swift", "Features/Challenge/ChallengeView.swift", "Challenge"),
    ("RoutinesView.swift", "Features/Routines/RoutinesView.swift", "Routines"),
    ("ReminderView.swift", "Features/Reminder/ReminderView.swift", "Reminder"),
    ("WattsView.swift", "Features/Watts/WattsView.swift", "Watts"),
]

resources = [
    ("Assets.xcassets", "Assets.xcassets", "folder.assetcatalog"),
    ("LaunchScreen.storyboard", "LaunchScreen.storyboard", "file.storyboard"),
]

config_files = [
    ("Info.plist", "Info.plist", "text.plist.xml"),
]

# Generate IDs
proj_id = gen_id("PROJECT")
target_id = gen_id("TARGET_WAKEOUT")
main_group_id = gen_id("GROUP_MAIN")
products_group_id = gen_id("GROUP_PRODUCTS")
app_product_id = gen_id("PRODUCT_APP")

sources_phase_id = gen_id("PHASE_SOURCES")
resources_phase_id = gen_id("PHASE_RESOURCES")
frameworks_phase_id = gen_id("PHASE_FRAMEWORKS")

cfg_proj_debug_id = gen_id("CFG_PROJ_DEBUG")
cfg_proj_release_id = gen_id("CFG_PROJ_RELEASE")
cfg_proj_list_id = gen_id("CFG_PROJ_LIST")

cfg_target_debug_id = gen_id("CFG_TARGET_DEBUG")
cfg_target_release_id = gen_id("CFG_TARGET_RELEASE")
cfg_target_list_id = gen_id("CFG_TARGET_LIST")

# Groups IDs
group_app_id = gen_id("GROUP_APP")
group_core_id = gen_id("GROUP_CORE")
group_camera_id = gen_id("GROUP_CAM")
group_motion_id = gen_id("GROUP_MOT")
group_features_id = gen_id("GROUP_FEAT")
group_games_id = gen_id("GROUP_GAMES")
group_challenge_id = gen_id("GROUP_CHALLENGE")
group_routines_id = gen_id("GROUP_ROUTINES")
group_reminder_id = gen_id("GROUP_REMINDER")
group_watts_id = gen_id("GROUP_WATTS")

# Build PBXBuildFile and PBXFileReference strings
pbx_build_files = []
pbx_file_refs = []

source_build_refs = []
resource_build_refs = []

# Process sources
for fname, fpath, grp in sources:
    fid = gen_id(f"FILEREF_{fname}")
    bid = gen_id(f"BUILDFILE_{fname}")
    pbx_file_refs.append(f'\t\t{fid} /* {fname} */ = {{isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = "{fname}"; sourceTree = "<group>"; }};')
    pbx_build_files.append(f'\t\t{bid} /* {fname} in Sources */ = {{isa = PBXBuildFile; fileRef = {fid} /* {fname} */; }};')
    source_build_refs.append(f'\t\t\t\t{bid} /* {fname} in Sources */,')

# Process resources
for rname, rpath, rtype in resources:
    fid = gen_id(f"FILEREF_{rname}")
    bid = gen_id(f"BUILDFILE_{rname}")
    pbx_file_refs.append(f'\t\t{fid} /* {rname} */ = {{isa = PBXFileReference; lastKnownFileType = {rtype}; path = "{rname}"; sourceTree = "<group>"; }};')
    pbx_build_files.append(f'\t\t{bid} /* {rname} in Resources */ = {{isa = PBXBuildFile; fileRef = {fid} /* {rname} */; }};')
    resource_build_refs.append(f'\t\t\t\t{bid} /* {rname} in Resources */,')

# Process configs
for cname, cpath, ctype in config_files:
    fid = gen_id(f"FILEREF_{cname}")
    pbx_file_refs.append(f'\t\t{fid} /* {cname} */ = {{isa = PBXFileReference; lastKnownFileType = {ctype}; path = "{cname}"; sourceTree = "<group>"; }};')

# Product reference
pbx_file_refs.append(f'\t\t{app_product_id} /* Wakeout.app */ = {{isa = PBXFileReference; explicitFileType = wrapper.application; includeInIndex = 0; path = Wakeout.app; sourceTree = BUILT_PRODUCTS_DIR; }};')

# Construct PBXGroups
pbx_groups = f'''
		{main_group_id} = {{
			isa = PBXGroup;
			children = (
				{group_app_id} /* App */,
				{group_core_id} /* Core */,
				{group_features_id} /* Features */,
				{gen_id("FILEREF_Assets.xcassets")} /* Assets.xcassets */,
				{gen_id("FILEREF_LaunchScreen.storyboard")} /* LaunchScreen.storyboard */,
				{gen_id("FILEREF_Info.plist")} /* Info.plist */,
				{products_group_id} /* Products */,
			);
			sourceTree = "<group>";
		}};
		{products_group_id} /* Products */ = {{
			isa = PBXGroup;
			children = (
				{app_product_id} /* Wakeout.app */,
			);
			name = Products;
			sourceTree = "<group>";
		}};
		{group_app_id} /* App */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_MoveDeskApp.swift")} /* MoveDeskApp.swift */,
			);
			path = App;
			sourceTree = "<group>";
		}};
		{group_core_id} /* Core */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_AppState.swift")} /* AppState.swift */,
				{gen_id("FILEREF_Theme.swift")} /* Theme.swift */,
				{group_camera_id} /* Camera */,
				{group_motion_id} /* Motion */,
			);
			path = Core;
			sourceTree = "<group>";
		}};
		{group_camera_id} /* Camera */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_MotionDetector.swift")} /* MotionDetector.swift */,
				{gen_id("FILEREF_CameraPreviewView.swift")} /* CameraPreviewView.swift */,
			);
			path = Camera;
			sourceTree = "<group>";
		}};
		{group_motion_id} /* Motion */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_GestureEngine.swift")} /* GestureEngine.swift */,
				{gen_id("FILEREF_Joint.swift")} /* Joint.swift */,
				{gen_id("FILEREF_OneEuroFilter.swift")} /* OneEuroFilter.swift */,
			);
			path = Motion;
			sourceTree = "<group>";
		}};
		{group_features_id} /* Features */ = {{
			isa = PBXGroup;
			children = (
				{group_games_id} /* Games */,
				{group_challenge_id} /* Challenge */,
				{group_routines_id} /* Routines */,
				{group_reminder_id} /* Reminder */,
				{group_watts_id} /* Watts */,
			);
			path = Features;
			sourceTree = "<group>";
		}};
		{group_games_id} /* Games */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_GameView.swift")} /* GameView.swift */,
			);
			path = Games;
			sourceTree = "<group>";
		}};
		{group_challenge_id} /* Challenge */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_ChallengeView.swift")} /* ChallengeView.swift */,
			);
			path = Challenge;
			sourceTree = "<group>";
		}};
		{group_routines_id} /* Routines */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_RoutinesView.swift")} /* RoutinesView.swift */,
			);
			path = Routines;
			sourceTree = "<group>";
		}};
		{group_reminder_id} /* Reminder */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_ReminderView.swift")} /* ReminderView.swift */,
			);
			path = Reminder;
			sourceTree = "<group>";
		}};
		{group_watts_id} /* Watts */ = {{
			isa = PBXGroup;
			children = (
				{gen_id("FILEREF_WattsView.swift")} /* WattsView.swift */,
			);
			path = Watts;
			sourceTree = "<group>";
		}};
'''

pbxproj_content = f'''// !$*UTF8*$!
{{
	archiveVersion = 1;
	classes = {{
	}};
	objectVersion = 56;
	objects = {{

/* Begin PBXBuildFile section */
{chr(10).join(pbx_build_files)}
/* End PBXBuildFile section */

/* Begin PBXFileReference section */
{chr(10).join(pbx_file_refs)}
/* End PBXFileReference section */

/* Begin PBXFrameworksBuildPhase section */
		{frameworks_phase_id} /* Frameworks */ = {{
			isa = PBXFrameworksBuildPhase;
			buildActionMask = 2147483647;
			files = (
			);
			runOnlyForDeploymentPostprocessing = 0;
		}};
/* End PBXFrameworksBuildPhase section */

/* Begin PBXGroup section */
{pbx_groups}
/* End PBXGroup section */

/* Begin PBXNativeTarget section */
		{target_id} /* Wakeout */ = {{
			isa = PBXNativeTarget;
			buildConfigurationList = {cfg_target_list_id} /* Build configuration list for PBXNativeTarget "Wakeout" */;
			buildPhases = (
				{sources_phase_id} /* Sources */,
				{frameworks_phase_id} /* Frameworks */,
				{resources_phase_id} /* Resources */,
			);
			buildRules = (
			);
			dependencies = (
			);
			name = Wakeout;
			productName = Wakeout;
			productReference = {app_product_id} /* Wakeout.app */;
			productType = "com.apple.product-type.application";
		}};
/* End PBXNativeTarget section */

/* Begin PBXProject section */
		{proj_id} /* Project object */ = {{
			isa = PBXProject;
			attributes = {{
				BuildIndependentTargetsInParallel = 1;
				LastSwiftUpdateCheck = 1520;
				LastUpgradeCheck = 1520;
				TargetAttributes = {{
					{target_id} = {{
						CreatedOnToolsVersion = 15.2;
					}};
				}};
			}};
			buildConfigurationList = {cfg_proj_list_id} /* Build configuration list for PBXProject "Wakeout" */;
			compatibilityVersion = "Xcode 14.0";
			developmentRegion = en;
			hasScannedForEncodings = 0;
			knownRegions = (
				en,
				Base,
			);
			mainGroup = {main_group_id};
			productRefGroup = {products_group_id} /* Products */;
			projectDirPath = "";
			projectRoot = "";
			targets = (
				{target_id} /* Wakeout */,
			);
		}};
/* End PBXProject section */

/* Begin PBXResourcesBuildPhase section */
		{resources_phase_id} /* Resources */ = {{
			isa = PBXResourcesBuildPhase;
			buildActionMask = 2147483647;
			files = (
{chr(10).join(resource_build_refs)}
			);
			runOnlyForDeploymentPostprocessing = 0;
		}};
/* End PBXResourcesBuildPhase section */

/* Begin PBXSourcesBuildPhase section */
		{sources_phase_id} /* Sources */ = {{
			isa = PBXSourcesBuildPhase;
			buildActionMask = 2147483647;
			files = (
{chr(10).join(source_build_refs)}
			);
			runOnlyForDeploymentPostprocessing = 0;
		}};
/* End PBXSourcesBuildPhase section */

/* Begin XCBuildConfiguration section */
		{cfg_proj_debug_id} /* Debug */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ALWAYS_SEARCH_USER_PATHS = NO;
				ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS = YES;
				CLANG_ANALYZER_NONNULL = YES;
				CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
				CLANG_CXX_LANGUAGE_STANDARD = "gnu++20";
				CLANG_ENABLE_MODULES = YES;
				CLANG_ENABLE_OBJC_ARC = YES;
				CLANG_ENABLE_OBJC_WEAK = YES;
				CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
				CLANG_WARN_BOOL_CONVERSION = YES;
				CLANG_WARN_COMMA = YES;
				CLANG_WARN_CONSTANT_CONVERSION = YES;
				CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
				CLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;
				CLANG_WARN_DOCUMENTATION_COMMENTS = YES;
				CLANG_WARN_EMPTY_BODY = YES;
				CLANG_WARN_ENUM_CONVERSION = YES;
				CLANG_WARN_INFINITE_RECURSION = YES;
				CLANG_WARN_INT_CONVERSION = YES;
				CLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
				CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
				CLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
				CLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
				CLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER = YES;
				CLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
				CLANG_WARN_STRICT_PROTOTYPES = YES;
				CLANG_WARN_SUSPICIOUS_MOVE = YES;
				CLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
				CLANG_WARN_UNREACHABLE_CODE = YES;
				CLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
				COPY_PHASE_STRIP = NO;
				DEBUG_INFORMATION_FORMAT = dwarf;
				ENABLE_STRICT_OBJC_MSGSEND = YES;
				ENABLE_TESTABILITY = YES;
				ENABLE_USER_SCRIPT_SANDBOXING = YES;
				GCC_C_LANGUAGE_STANDARD = gnu17;
				GCC_DYNAMIC_NO_PIC = NO;
				GCC_NO_COMMON_BLOCKS = YES;
				GCC_OPTIMIZATION_LEVEL = 0;
				GCC_PREPROCESSOR_DEFINITIONS = (
					"DEBUG=1",
					"$(inherited)",
				);
				GCC_WARN_64_TO_32_BIT_CONVERSION = YES;
				GCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
				GCC_WARN_UNDEFINED_VARIABLES = YES;
				GCC_WARN_UNUSED_FUNCTION = YES;
				GCC_WARN_UNUSED_VARIABLE = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 16.0;
				MTL_ENABLE_DEBUG_INFO = INCLUDE_SOURCE;
				MTL_FAST_MATH = YES;
				ONLY_ACTIVE_ARCH = YES;
				SDKROOT = iphoneos;
				SWIFT_ACTIVE_COMPILATION_CONDITIONS = "DEBUG $(inherited)";
				SWIFT_OPTIMIZATION_LEVEL = "-Onone";
			}};
			name = Debug;
		}};
		{cfg_proj_release_id} /* Release */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ALWAYS_SEARCH_USER_PATHS = NO;
				ASSETCATALOG_COMPILER_GENERATE_SWIFT_ASSET_SYMBOL_EXTENSIONS = YES;
				CLANG_ANALYZER_NONNULL = YES;
				CLANG_ANALYZER_NUMBER_OBJECT_CONVERSION = YES_AGGRESSIVE;
				CLANG_CXX_LANGUAGE_STANDARD = "gnu++20";
				CLANG_ENABLE_MODULES = YES;
				CLANG_ENABLE_OBJC_ARC = YES;
				CLANG_ENABLE_OBJC_WEAK = YES;
				CLANG_WARN_BLOCK_CAPTURE_AUTORELEASING = YES;
				CLANG_WARN_BOOL_CONVERSION = YES;
				CLANG_WARN_COMMA = YES;
				CLANG_WARN_CONSTANT_CONVERSION = YES;
				CLANG_WARN_DEPRECATED_OBJC_IMPLEMENTATIONS = YES;
				CLANG_WARN_DIRECT_OBJC_ISA_USAGE = YES_ERROR;
				CLANG_WARN_DOCUMENTATION_COMMENTS = YES;
				CLANG_WARN_EMPTY_BODY = YES;
				CLANG_WARN_ENUM_CONVERSION = YES;
				CLANG_WARN_INFINITE_RECURSION = YES;
				CLANG_WARN_INT_CONVERSION = YES;
				CLANG_WARN_NON_LITERAL_NULL_CONVERSION = YES;
				CLANG_WARN_OBJC_IMPLICIT_RETAIN_SELF = YES;
				CLANG_WARN_OBJC_LITERAL_CONVERSION = YES;
				CLANG_WARN_OBJC_ROOT_CLASS = YES_ERROR;
				CLANG_WARN_QUOTED_INCLUDE_IN_FRAMEWORK_HEADER = YES;
				CLANG_WARN_RANGE_LOOP_ANALYSIS = YES;
				CLANG_WARN_STRICT_PROTOTYPES = YES;
				CLANG_WARN_SUSPICIOUS_MOVE = YES;
				CLANG_WARN_UNGUARDED_AVAILABILITY = YES_AGGRESSIVE;
				CLANG_WARN_UNREACHABLE_CODE = YES;
				CLANG_WARN__DUPLICATE_METHOD_MATCH = YES;
				COPY_PHASE_STRIP = NO;
				DEBUG_INFORMATION_FORMAT = "dwarf-with-dsym";
				ENABLE_NS_ASSERTIONS = NO;
				ENABLE_STRICT_OBJC_MSGSEND = YES;
				ENABLE_USER_SCRIPT_SANDBOXING = YES;
				GCC_C_LANGUAGE_STANDARD = gnu17;
				GCC_NO_COMMON_BLOCKS = YES;
				GCC_WARN_64_TO_32_BIT_CONVERSION = YES;
				GCC_WARN_ABOUT_RETURN_TYPE = YES_ERROR;
				GCC_WARN_UNDEFINED_VARIABLES = YES;
				GCC_WARN_UNUSED_FUNCTION = YES;
				GCC_WARN_UNUSED_VARIABLE = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 16.0;
				MTL_ENABLE_DEBUG_INFO = NO;
				MTL_FAST_MATH = YES;
				SDKROOT = iphoneos;
				SWIFT_COMPILATION_MODE = "wholemodule";
				SWIFT_OPTIMIZATION_LEVEL = "-O";
				VALIDATE_PRODUCT = YES;
			}};
			name = Release;
		}};
		{cfg_target_debug_id} /* Debug */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
				ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
				CODE_SIGN_STYLE = Automatic;
				CURRENT_PROJECT_VERSION = 1;
				DEVELOPMENT_ASSET_PATHS = "";
				ENABLE_PREVIEWS = YES;
				GENERATE_INFOPLIST_FILE = NO;
				INFOPLIST_FILE = Info.plist;
				INFOPLIST_KEY_CFBundleDisplayName = Wakeout;
				INFOPLIST_KEY_LSApplicationCategoryType = "public.app-category.healthcare-fitness";
				INFOPLIST_KEY_UIApplicationSceneManifest_Generation = YES;
				INFOPLIST_KEY_UILaunchStoryboardName = LaunchScreen;
				INFOPLIST_KEY_UIRequiresFullScreen = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 16.0;
				LD_RUNPATH_SEARCH_PATHS = (
					"$(inherited)",
					"@executable_path/Frameworks",
				);
				MARKETING_VERSION = 1.0.0;
				PRODUCT_BUNDLE_IDENTIFIER = com.breakpulse.app;
				PRODUCT_NAME = Wakeout;
				SWIFT_EMIT_LOC_STRINGS = YES;
				SWIFT_VERSION = 5.0;
				TARGETED_DEVICE_FAMILY = "1,2";
			}};
			name = Debug;
		}};
		{cfg_target_release_id} /* Release */ = {{
			isa = XCBuildConfiguration;
			buildSettings = {{
				ASSETCATALOG_COMPILER_APPICON_NAME = AppIcon;
				ASSETCATALOG_COMPILER_GLOBAL_ACCENT_COLOR_NAME = AccentColor;
				CODE_SIGN_STYLE = Automatic;
				CURRENT_PROJECT_VERSION = 1;
				DEVELOPMENT_ASSET_PATHS = "";
				ENABLE_PREVIEWS = YES;
				GENERATE_INFOPLIST_FILE = NO;
				INFOPLIST_FILE = Info.plist;
				INFOPLIST_KEY_CFBundleDisplayName = Wakeout;
				INFOPLIST_KEY_LSApplicationCategoryType = "public.app-category.healthcare-fitness";
				INFOPLIST_KEY_UIApplicationSceneManifest_Generation = YES;
				INFOPLIST_KEY_UILaunchStoryboardName = LaunchScreen;
				INFOPLIST_KEY_UIRequiresFullScreen = YES;
				IPHONEOS_DEPLOYMENT_TARGET = 16.0;
				LD_RUNPATH_SEARCH_PATHS = (
					"$(inherited)",
					"@executable_path/Frameworks",
				);
				MARKETING_VERSION = 1.0.0;
				PRODUCT_BUNDLE_IDENTIFIER = com.breakpulse.app;
				PRODUCT_NAME = Wakeout;
				SWIFT_EMIT_LOC_STRINGS = YES;
				SWIFT_VERSION = 5.0;
				TARGETED_DEVICE_FAMILY = "1,2";
			}};
			name = Release;
		}};
/* End XCBuildConfiguration section */

/* Begin XCConfigurationList section */
		{cfg_proj_list_id} /* Build configuration list for PBXProject "Wakeout" */ = {{
			isa = XCConfigurationList;
			buildConfigurations = (
				{cfg_proj_debug_id} /* Debug */,
				{cfg_proj_release_id} /* Release */,
			);
			defaultConfigurationIsVisible = 0;
			defaultConfigurationName = Release;
		}};
		{cfg_target_list_id} /* Build configuration list for PBXNativeTarget "Wakeout" */ = {{
			isa = XCConfigurationList;
			buildConfigurations = (
				{cfg_target_debug_id} /* Debug */,
				{cfg_target_release_id} /* Release */,
			);
			defaultConfigurationIsVisible = 0;
			defaultConfigurationName = Release;
		}};
/* End XCConfigurationList section */

	}};
	rootObject = {proj_id} /* Project object */;
}}
'''

with open(os.path.join(proj_dir, "project.pbxproj"), "w", encoding="utf-8") as f:
    f.write(pbxproj_content)

# 2. Generate xcscheme for 1-click Run in Xcode
scheme_content = f'''<?xml version="1.0" encoding="UTF-8"?>
<Scheme
   LastUpgradeVersion = "1520"
   version = "1.7">
   <BuildAction
      parallelizeBuildables = "YES"
      buildImplicitDependencies = "YES">
      <BuildActionEntries>
         <BuildActionEntry
            buildForTesting = "YES"
            buildForRunning = "YES"
            buildForProfiling = "YES"
            buildForArchiving = "YES"
            buildForAnalyzing = "YES">
            <BuildableReference
               BuildableIdentifier = "primary"
               BlueprintIdentifier = "{target_id}"
               BuildableName = "Wakeout.app"
               BlueprintName = "Wakeout"
               ReferencedContainer = "container:Wakeout.xcodeproj">
            </BuildableReference>
         </BuildActionEntry>
      </BuildActionEntries>
   </BuildAction>
   <TestAction
      buildConfiguration = "Debug"
      selectedDebuggerIdentifier = "Xcode.DebuggerFoundation.Debugger.LLDB"
      selectedLauncherIdentifier = "Xcode.DebuggerFoundation.Launcher.LLDB"
      shouldUseLaunchSchemeArgsEnv = "YES"
      shouldAutocreateTestPlan = "YES">
   </TestAction>
   <LaunchAction
      buildConfiguration = "Debug"
      selectedDebuggerIdentifier = "Xcode.DebuggerFoundation.Debugger.LLDB"
      selectedLauncherIdentifier = "Xcode.DebuggerFoundation.Launcher.LLDB"
      launchStyle = "0"
      useCustomWorkingDirectory = "NO"
      ignoresPersistentStateOnLaunch = "NO"
      debugDocumentVersioning = "YES"
      debugServiceExtension = "internal"
      allowLocationSimulation = "YES">
      <BuildableProductRunnable
         runnableDebuggingMode = "0">
         <BuildableReference
            BuildableIdentifier = "primary"
            BlueprintIdentifier = "{target_id}"
            BuildableName = "Wakeout.app"
            BlueprintName = "Wakeout"
            ReferencedContainer = "container:Wakeout.xcodeproj">
         </BuildableReference>
      </BuildableProductRunnable>
   </LaunchAction>
   <ProfileAction
      buildConfiguration = "Release"
      shouldUseLaunchSchemeArgsEnv = "YES"
      savedToolIdentifier = ""
      useCustomWorkingDirectory = "NO"
      debugDocumentVersioning = "YES">
      <BuildableProductRunnable
         runnableDebuggingMode = "0">
         <BuildableReference
            BuildableIdentifier = "primary"
            BlueprintIdentifier = "{target_id}"
            BuildableName = "Wakeout.app"
            BlueprintName = "Wakeout"
            ReferencedContainer = "container:Wakeout.xcodeproj">
         </BuildableReference>
      </BuildableProductRunnable>
   </ProfileAction>
   <AnalyzeAction
      buildConfiguration = "Debug">
   </AnalyzeAction>
   <ArchiveAction
      buildConfiguration = "Release"
      revealArchiveInOrganizer = "YES">
   </ArchiveAction>
</Scheme>
'''

with open(os.path.join(shared_schemes_dir, "Wakeout.xcscheme"), "w", encoding="utf-8") as f:
    f.write(scheme_content)

print("Successfully generated Wakeout.xcodeproj and shared schemes!")
