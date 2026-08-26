# Compose UI — feature modules, screens, navigation

Full detail for `28-compose-ui.md`. Canonical app: **AnimeHub** (`E:\SohaibAhmed\Github\AnimeHub`). Do not invent a parallel Compose layout.

Applies when `.claude/project-settings.json` `uiFramework` is `"compose"`.

MVI ViewModel law is unchanged — see `04-mvi-presentation` + [mvi-presentation.md](mvi-presentation.md). This file covers **Compose packaging and UI**.

## Modules

```
app                 Application, KoinModules, MainActivity, navigation/NavGraph.kt
domain              Entities, repository interfaces, UseCases
data                Repository impls, DataSources
core-common         Constants, EventsProvider
core-ui             strings.xml, drawables, splash XML theme
core-design         Color.kt, Type.kt, Theme.kt (AppTheme)
core-platform       Firebase, InternetManager, dispatchers
feature-entrance    Start destination (template invariant — not Splash)
feature-<name>      One screen cluster per module
```

- **No `:presentation` module** on Compose projects
- Gradle names: kebab-case (`:feature-anime-details`)
- Namespace: `{applicationId}.feature.<name>` (dots)
- `:app` `implementation` every `:feature-*`; features do **not** depend on each other
- Tab / dashboard hosts accept `@Composable () -> Unit` slots; `:app` `NavGraph` wires children (AnimeHub `DashboardScreen`)

## Feature package

```
feature-home/src/main/java/…/feature/home/
  HomeScreen.kt                 # HOME_ROUTE + HomeScreen + HomeScreenContent + items/previews
  di/HomeFeatureModule.kt       # lazyModule { viewModel { HomeViewModel(get(), …) } }
  intent/HomeIntent.kt
  state/HomeState.kt
  effect/HomeEffect.kt
  viewModel/HomeViewModel.kt
  components/                   # optional shared items / dialogs / sheets
```

DI val name: `homeFeatureModule` (feature + `FeatureModule`). Register in `:app` `KoinModules` `featureList`.

## Naming

| Kind | Rule | Example |
|------|------|---------|
| Screen | `<Feature>Screen` | `HomeScreen`, `AnimeDetailsScreen` |
| Content | private `<Feature>ScreenContent` | `HomeScreenContent` |
| States | private `<Feature>LoadingContent` / `Empty` / `Error` / `Success` | `HomeEmptyContent` |
| List row | `<Name>Item` | `AnimeItem` |
| Dialog | `<Feature><Purpose>Dialog` | `HomeFilterDialog` |
| Sheet | `<Feature><Purpose>BottomSheet` | `HomeSortBottomSheet` |
| Route | `FEATURE_ROUTE` snake string | `const val HOME_ROUTE = "home"` |
| Route with args | pattern + factory | `ANIME_DETAILS_ROUTE = "anime_details/{animeId}"` + `createRouteAnimeDetails(id)` |
| Nested tab routes | `parent/child` | `dashboard/home` |
| Theme | `{App}Theme` in `:core-design` | `AppTheme` / `AnimeHubTheme` |

## Screen template

```kotlin
const val HOME_ROUTE = "home"

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToDetail: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToDetail -> onNavigateToDetail(effect.id)
            }
        }
    }

    HomeScreenContent(
        modifier = modifier,
        state = state,
        onIntent = viewModel::handleIntent,
    )
}

@Composable
private fun HomeScreenContent(
    modifier: Modifier = Modifier,
    state: HomeState,
    onIntent: (HomeIntent) -> Unit,
) { /* Material3; dispatch onIntent(HomeIntent.OnItemClick(id)) */ }
```

- Collect state with `collectAsStateWithLifecycle` — not `collectAsState`
- Effects: `LaunchedEffect(viewModel) { viewModel.effect.collect { … } }`
- `Modifier` defaults to `Modifier`; pass `modifier.fillMaxSize()` from NavHost when needed
- No `NavController` / `LocalContext` navigation in the feature — callbacks only
- ViewModel stays project MVI: `handleIntent` + `suspend onX()` + `handleError` last (`04`)

## Root NavGraph (`:app`)

```kotlin
@Composable
fun NavGraph(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ENTRANCE_ROUTE,
        modifier = modifier.fillMaxSize(),
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        composable(ENTRANCE_ROUTE) {
            EntranceScreen(
                navigateToHome = {
                    navController.navigate(HOME_ROUTE) {
                        popUpTo(ENTRANCE_ROUTE) { inclusive = true }
                    }
                },
            )
        }
        composable(HOME_ROUTE) {
            HomeScreen(onNavigateToDetail = { navController.navigate(createRouteDetail(it)) })
        }
        composable(
            route = DETAIL_ROUTE,
            arguments = listOf(navArgument("id") { type = NavType.StringType }),
        ) { entry ->
            DetailScreen(
                id = entry.arguments?.getString("id"),
                onNavigateBack = { navController.navigateUp() },
            )
        }
    }
}
```

- Start destination is **Entrance** (`ENTRANCE_ROUTE`) — same product invariant as XML `entranceFragment`
- Slide transitions match XML `slide_in_right` / `slide_out_left` semantics
- Nested `NavHost` for bottom tabs (AnimeHub dashboard): `popUpTo(start) { saveState = true }`, `launchSingleTop`, `restoreState`

## MainActivity (`:app`)

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                NavGraph()
            }
        }
    }
}
```

- **Never** `GlobalContext.get()` for theme/prefs — `koinInject()` inside a composable, or a small wrapper that collects a Flow
- Theme / DynamicColors still applied after `startKoin` in `Application` (`07`, `23`)

## Gradle (Compose modules)

`:app` and every `:feature-*` / `:core-design`:

```kotlin
plugins {
    alias(libs.plugins.android.library) // or android.application on :app
    alias(libs.plugins.kotlin.compose)
}
android {
    buildFeatures { compose = true }
}
dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.koin.androidx.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

`:app` also: `activity-compose`, `navigation-compose`. Images: Coil 3 (`coil-compose` + `coil-network-okhttp`) on feature modules that load images — **not** Glide.

Catalog keys (latest stable on add): `composeBom`, `navigationCompose`, `activityCompose`, `coilCompose`, plugin `kotlin-compose` (`org.jetbrains.kotlin.plugin.compose` — Compose Compiler, **not** `kotlin-android`).

Do **not** enable View Binding on Compose-only feature modules. XML `:core-ui` may still use resources without View Binding if it has no layouts.

## Lists

- `LazyColumn` / `LazyVerticalGrid` with stable `key`
- Paging: `collectAsLazyPagingItems` when the feature uses Paging 3
- Do not skip placeholder slots in a keyed grid (keep order)
- Heavy map/filter/sort stays in Repo/UseCase/ViewModel — composables bind `*UiItem` / state fields only

## Dialogs and sheets

- Dialog: `AlertDialog` / `Dialog` composable named `*Dialog` — not `DialogFragment`
- Sheet: `ModalBottomSheet` named `*BottomSheet` — not `BottomSheetDialogFragment`
- Hoist visibility in State (`showFilterSheet`) or pass `onDismiss` lambdas; ViewModel owns the flag via Intent

## Theme (`:core-design`)

- `Color.kt` — light/dark scheme colors from design tokens (`hsc-setup-design-system`)
- `Type.kt` — `Typography`
- `Theme.kt` — `@Composable fun AppTheme(darkTheme, dynamicColor, content)` wrapping `MaterialTheme`
- XML `themes.xml` in `:core-ui` still sets `android:windowBackground` / splash (`Theme.App.Starting`)

## Strings and resources

- All user-facing strings stay in **one** `:core-ui` `strings.xml` (App → General → `cd_*` → Screen-wise)
- Features `implementation(project(":core-ui"))` and `stringResource(R.string.*)`
- Drawables in `:core-ui`; Compose reads with `painterResource` / Coil

## Forbidden

- XML `fragment_*` screens, View Binding, Data Binding, `findViewById` for feature UI
- `NavController` in ViewModel or inside `*ScreenContent`
- Feature → `:data` or feature → feature Gradle deps
- `GlobalContext` in Activity / composables
- Hardcoded copy; `dimens.xml`; random hex that duplicates `AppTheme`
- Glide in Compose features (use Coil)
- Converting ads (`:gmaAds`) to Compose MVI unless the user explicitly asks
