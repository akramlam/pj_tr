# Points Saillants du Code - Application Binome Matcher

## 1. JEE - Java Enterprise Edition

### 1.1 Configuration d'Application Spring Boot
**Localisation :** `api/src/main/java/com/example/api/BinomeMatcherApiApplication.java`

```java
@SpringBootApplication(scanBasePackages = "com.example")
@EntityScan(basePackages = "com.example.core.domain")
@EnableJpaRepositories(basePackages = "com.example.api.repository")
public class BinomeMatcherApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BinomeMatcherApiApplication.class, args);
    }
}
```

**Utilisation :** Configure l'application Spring Boot avec le scan des composants, le scan des entités JPA, et l'activation des repositories à travers plusieurs modules.

### 1.1 Services Web RESTful
**Localisation :** `api/src/main/java/com/example/api/controller/MatchController.java`

```java
@RestController
@RequestMapping("/api/matches")
public class MatchController {
    private final MatchService matchService;

    @GetMapping("/potential")
    public ResponseEntity<List<PotentialMatchDto>> getPotentialMatches(Principal principal) {
        List<MatchDto> matches = matchService.findMatches(principal.getName(), 20);
        List<PotentialMatchDto> potentialMatches = matches.stream()
                .map(match -> {
                    PotentialMatchDto dto = new PotentialMatchDto();
                    dto.userId = match.getUserId();
                    dto.username = match.getUsername();
                    dto.compatibilityScore = Math.min(95, match.getScore() * 15 + 40);
                    return dto;`
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(potentialMatches);
    }
}
```

**Utilisation :** Expose les endpoints HTTP pour la consommation client, gérant la récupération des correspondances avec les codes de statut HTTP appropriés et les réponses JSON.

### 1.3 Mappage d'Entité JPA
**Localisation :** `core/src/main/java/com/example/core/domain/Profile.java`

```java
@Entity
@Table(name = "profiles")
public class Profile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private Set<String> skills = new HashSet<>();
}
```

**Utilisation :** Mappe les objets Java aux tables de base de données avec des relations, permettant la persistance objet-relationnelle pour les profils utilisateur et les compétences.

### 1.4 Configuration de Sécurité avec JWT
**Localisation :** `security/src/main/java/com/example/security/jwt/JwtTokenProvider.java`

```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(now)
            .setExpiration(expiry)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

**Utilisation :** Implémente l'authentification sans état utilisant les tokens JWT, fournissant une gestion sécurisée des sessions utilisateur sans stockage côté serveur.

### 1.5 Injection de Dépendances
**Localisation :** `api/src/main/java/com/example/api/service/AuthenticationService.java`

```java
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticationService(UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }
}
```

**Utilisation :** Injecte les dépendances par injection de constructeur, favorisant le faible couplage et facilitant les tests unitaires.

## 2. Développement Mobile - Android

### 2.1 Gestion du Cycle de Vie d'Activité
**Localisation :** `client/app/src/main/java/com/example/client/MainActivity.java`

```java
public class MainActivity extends AppCompatActivity {
    private ApiClient apiClient;
    private DebugLogger debugLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        debugLogger = DebugLogger.getInstance(this);
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onCreate started");
        
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        apiClient = ApiClient.getInstance(this);
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        debugLogger.logScreenView("MAIN");
    }
}
```

**Utilisation :** Gère le cycle de vie de l'activité Android, initialise les composants principaux, et configure le tiroir de navigation pour l'interface utilisateur principale.

### 2.2 Client HTTP avec Intercepteur d'Authentification
**Localisation :** `client/app/src/main/java/com/example/client/api/ApiClient.java`

```java
Interceptor authInterceptor = new Interceptor() {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        String path = original.url().encodedPath();
        if (path.contains("/auth/login") || path.contains("/auth/register")) {
            return chain.proceed(original);
        }
        
        String token = getToken();
        if (token != null) {
            Request authenticated = original.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(authenticated);
        }
        
        return chain.proceed(original);
    }
};
```

**Utilisation :** Ajoute automatiquement les en-têtes d'authentification aux requêtes HTTP, implémentant le patron Chaîne de Responsabilité pour le traitement des requêtes.

### 2.3 Préférences Partagées pour la Persistance des Données
**Localisation :** `client/app/src/main/java/com/example/client/api/ApiClient.java`

```java
public void saveToken(String token) {
    sharedPreferences.edit().putString(TOKEN_KEY, token).apply();
}

public String getToken() {
    return sharedPreferences.getString(TOKEN_KEY, null);
}

public boolean isLoggedIn() {
    return getToken() != null;
}
```

**Utilisation :** Persiste les tokens d'authentification utilisateur localement sur l'appareil, permettant la gestion des sessions à travers les redémarrages d'application.

### 2.4 Intégration du Composant de Navigation
**Localisation :** `client/app/src/main/java/com/example/client/MainActivity.java`

```java
mAppBarConfiguration = new AppBarConfiguration.Builder(
        R.id.nav_dashboard, R.id.nav_profile, R.id.nav_matches, R.id.nav_messages)
        .setOpenableLayout(drawer)
        .build();

NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
NavigationUI.setupWithNavController(navigationView, navController);
```

**Utilisation :** Implémente le Composant de Navigation Android pour une navigation fluide entre fragments avec intégration du layout de tiroir.

## Résumé de l'Architecture Technique

### Structure Multi-Modules
- **Module Core :** Entités de domaine et logique métier partagée
- **Module API :** Contrôleurs REST et services métier
- **Module Security :** Composants d'authentification et d'autorisation
- **Module Client :** Application Android avec composants UI

### Intégration des Technologies Clés
- **Spring Boot :** Framework d'application d'entreprise
- **Spring Security :** Authentification et autorisation
- **JPA/Hibernate :** Mappage objet-relationnel
- **JWT :** Tokens d'authentification sans état
- **Navigation Android :** Navigation UI Android moderne
- **Retrofit/OkHttp :** Client HTTP pour communication API
- **SharedPreferences :** Persistance de données locale

Cette architecture démontre l'intégration du développement Java d'entreprise avec le développement d'applications mobiles modernes, suivant les patrons de conception établis et les meilleures pratiques.