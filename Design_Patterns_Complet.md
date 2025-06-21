# Design Patterns Utilisés dans le Projet Binome Matcher

Ce document présente une analyse complète des design patterns (patrons de conception) utilisés dans le projet Binome Matcher, avec les exemples de code correspondants.

## Table des Matières

1. [Pattern Singleton](#pattern-singleton)
2. [Pattern Repository](#pattern-repository)
3. [Pattern Service Layer](#pattern-service-layer)
4. [Pattern MVC (Model-View-Controller)](#pattern-mvc)
5. [Pattern DTO (Data Transfer Object)](#pattern-dto)
6. [Pattern Factory](#pattern-factory)
7. [Pattern Chain of Responsibility](#pattern-chain-of-responsibility)
8. [Pattern Filter](#pattern-filter)
9. [Pattern Dependency Injection](#pattern-dependency-injection)
10. [Pattern Builder](#pattern-builder)

---

## Pattern Singleton

**Localisation :** `client/app/src/main/java/com/example/client/api/ApiClient.java`

**Objectif :** Garantir qu'une seule instance de la classe ApiClient existe dans l'application Android.

**Implémentation :**

```java
public class ApiClient {
    private static ApiClient instance;
    
    private ApiClient(Context context) {
        // Constructeur privé pour empêcher l'instanciation directe
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // ... initialisation
    }
    
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }
}
```

**Avantages :**
- Une seule instance du client API dans toute l'application
- Gestion centralisée des tokens d'authentification
- Évite les fuites mémoire en utilisant le contexte applicatif

---

## Pattern Repository

**Localisation :** `api/src/main/java/com/example/api/repository/`

**Objectif :** Encapsuler la logique d'accès aux données et fournir une interface uniforme pour les opérations CRUD.

**Implémentation :**

```java
// Interface Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}

// Utilisation dans le service
@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    
    public AuthenticationService(UserRepository userRepository, ...) {
        this.userRepository = userRepository;
    }
    
    public String register(String username, String email, String password) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username is already taken");
        }
        // ...
    }
}
```

**Autres repositories :**
- `ProfileRepository.java`
- `MessageRepository.java`
- `DebugLogRepository.java`

**Avantages :**
- Séparation claire entre la logique métier et l'accès aux données
- Facilite les tests unitaires (mocking)
- Abstraction de la base de données

---

## Pattern Service Layer

**Localisation :** `api/src/main/java/com/example/api/service/`

**Objectif :** Encapsuler la logique métier et coordonner les opérations entre différentes couches.

**Implémentation :**

```java
@Service
public class MatchService {
    private final ProfileRepository profileRepository;

    public MatchService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Transactional(readOnly = true)
    public List<MatchDto> findMatches(String username, int limit) {
        // Logique métier pour trouver les correspondances
        List<Profile> all = profileRepository.findAll();
        Profile me = all.stream()
                .filter(p -> username.equals(p.getUser().getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Calcul des correspondances basé sur les compétences communes
        // ...
        return matches;
    }
}
```

**Services disponibles :**
- `AuthenticationService` - Gestion de l'authentification
- `MatchService` - Algorithme de matching
- `ProfileService` - Gestion des profils
- `MessageService` - Gestion des messages
- `DebugLogService` - Logging et débogage

**Avantages :**
- Logique métier centralisée
- Réutilisabilité du code
- Facilite la maintenance

---

## Pattern MVC (Model-View-Controller)

**Localisation :** Architecture globale du projet

**Objectif :** Séparer la logique de présentation, la logique métier et les données.

**Implémentation :**

### Controller (Contrôleur)
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        String token = authService.register(request.getUsername(), 
                                          request.getEmail(), 
                                          request.getPassword());
        return ResponseEntity.ok(new AuthResponse(token));
    }
}
```

### Model (Modèle)
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

    @Column(nullable = false)
    private String formation;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "profile_skills")
    private Set<String> skills = new HashSet<>();
    
    // getters et setters...
}
```

### View (Vue)
Côté Android : `fragment_matches.xml`, `fragment_profile.xml`, etc.

**Avantages :**
- Séparation claire des responsabilités
- Facilite la maintenance et les tests
- Réutilisabilité des composants

---

## Pattern DTO (Data Transfer Object)

**Localisation :** Dans les contrôleurs et services

**Objectif :** Transférer des données entre les couches sans exposer la structure interne des entités.

**Implémentation :**

```java
// DTO interne dans MatchService
public static class MatchDto {
    private String username;
    private String formation;
    private Set<String> skills;
    private String preferences;
    private int score;

    public MatchDto(String username, String formation, Set<String> skills, 
                   String preferences, int score) {
        this.username = username;
        this.formation = formation;
        this.skills = skills;
        this.preferences = preferences;
        this.score = score;
    }
    
    // getters...
}

// DTOs dans les contrôleurs
public static class RegisterRequest {
    private String username;
    private String email;
    private String password;
    // getters et setters...
}

public static class AuthResponse {
    private String token;
    
    public AuthResponse(String token) { 
        this.token = token; 
    }
    // getters et setters...
}
```

**Avantages :**
- Contrôle de l'exposition des données
- Optimisation des transferts réseau
- Versioning des APIs

---

## Pattern Factory

**Localisation :** Configuration Spring et création d'objets

**Objectif :** Créer des objets sans spécifier leurs classes exactes.

**Implémentation :**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Factory method pour créer la chaîne de filtres de sécurité
        return http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**Dans le client Android :**
```java
// Factory pattern dans ApiClient pour Retrofit
Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

apiService = retrofit.create(BinomeApiService.class);
```

**Avantages :**
- Flexibilité dans la création d'objets
- Configuration centralisée
- Facilite les tests et le mocking

---

## Pattern Chain of Responsibility

**Localisation :** Filtres de sécurité Spring et intercepteurs

**Objectif :** Faire passer une requête à travers une chaîne de gestionnaires.

**Implémentation :**

```java
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
        throws ServletException, IOException {

        String token = getJwtFromRequest(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        // Passer au filtre suivant dans la chaîne
        filterChain.doFilter(request, response);
    }
}
```

**Dans le client Android (OkHttp Interceptors) :**
```java
Interceptor authInterceptor = new Interceptor() {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        
        // Traitement de l'authentification
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

**Avantages :**
- Flexibilité dans le traitement des requêtes
- Facilite l'ajout/suppression de traitements
- Découplage des responsabilités

---

## Pattern Filter

**Localisation :** Sécurité Spring et client HTTP

**Objectif :** Intercepter et traiter les requêtes avant qu'elles n'atteignent leur destination.

**Implémentation :**

Voir l'exemple du `JwtAuthenticationFilter` dans la section Chain of Responsibility.

**Configuration :**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
        // ... configuration
        .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

**Avantages :**
- Traitement transversal des requêtes
- Sécurité centralisée
- Logging et monitoring

---

## Pattern Dependency Injection

**Localisation :** Partout dans le projet Spring

**Objectif :** Injecter les dépendances plutôt que de les créer manuellement.

**Implémentation :**

```java
@Service
public class AuthenticationService {
    // Injection par constructeur (recommandée)
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

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authService;

    // Injection par constructeur
    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }
}
```

**Avantages :**
- Facilite les tests unitaires
- Réduction du couplage
- Gestion automatique du cycle de vie des objets

---

## Pattern Builder

**Localisation :** Configuration et création d'objets complexes

**Objectif :** Construire des objets complexes étape par étape.

**Implémentation :**

```java
// Dans ApiClient (Android)
OkHttpClient httpClient = new OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(logging)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(false)
        .build();

Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build();

// Dans MainActivity (Android)
mAppBarConfiguration = new AppBarConfiguration.Builder(
        R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, 
        R.id.nav_matches, R.id.nav_messages, R.id.nav_profile, R.id.nav_settings)
        .setOpenableLayout(drawer)
        .build();
```

**Avantages :**
- Construction d'objets complexes de manière lisible
- Flexibilité dans la configuration
- Code plus maintenable

---

## Conclusion

Ce projet utilise efficacement plusieurs design patterns pour créer une architecture robuste et maintenable :

### Patterns Structurels
- **Singleton** : Gestion unique du client API
- **Repository** : Abstraction de l'accès aux données
- **MVC** : Séparation claire des responsabilités

### Patterns Comportementaux
- **Chain of Responsibility** : Traitement des filtres de sécurité
- **Service Layer** : Encapsulation de la logique métier

### Patterns Créationnels
- **Factory** : Configuration Spring et création d'objets
- **Builder** : Construction d'objets complexes
- **Dependency Injection** : Gestion des dépendances

### Patterns de Transfert
- **DTO** : Transfert sécurisé des données entre couches

Ces patterns contribuent à :
- **Maintenabilité** : Code organisé et modulaire
- **Testabilité** : Isolation des composants
- **Réutilisabilité** : Composants découplés
- **Sécurité** : Gestion centralisée de l'authentification
- **Performance** : Optimisation des accès données et réseau

Le projet démontre une bonne compréhension des principes SOLID et des bonnes pratiques de développement en utilisant ces patterns de manière appropriée selon le contexte.