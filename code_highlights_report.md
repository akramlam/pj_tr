# Code Highlights - Binome Matcher Application

## 1. Génie Logiciel - Design Patterns

### 1.1 Singleton Pattern
**Location:** `client/app/src/main/java/com/example/client/api/ApiClient.java`

```java
public class ApiClient {
    private static ApiClient instance;
    
    private ApiClient(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        // Configuration du client HTTP
    }
    
    public static synchronized ApiClient getInstance(Context context) {
        if (instance == null) {
            instance = new ApiClient(context.getApplicationContext());
        }
        return instance;
    }
}
```

**Usage:** Ensures a single API client instance throughout the Android application, managing authentication tokens and HTTP configurations centrally.

### 1.2 Repository Pattern
**Location:** `api/src/main/java/com/example/api/repository/UserRepository.java`

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
}
```

**Usage:** Abstracts data access operations, providing a clean interface between business logic and database operations.

### 1.3 Service Layer Pattern
**Location:** `api/src/main/java/com/example/api/service/MatchService.java`

```java
@Service
public class MatchService {
    private final ProfileRepository profileRepository;

    @Transactional(readOnly = true)
    public List<MatchDto> findMatches(String username, int limit) {
        // Core matching algorithm implementation
        List<Profile> all = profileRepository.findAll();
        Profile me = all.stream()
                .filter(p -> username.equals(p.getUser().getUsername()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        // Calculate compatibility based on shared skills
        Set<String> mySkills = new HashSet<>(me.getSkills());
        for (Profile p : all) {
            Set<String> common = new HashSet<>(mySkills);
            common.retainAll(p.getSkills());
            int compatibilityScore = common.size();
            // Add to matches if compatible
        }
        return matches.stream()
                .sorted(Comparator.comparingInt(MatchDto::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }
}
```

**Usage:** Encapsulates the core business logic for matching users based on skills and formation, implementing the application's main functionality.

### 1.4 DTO Pattern
**Location:** `api/src/main/java/com/example/api/controller/AuthController.java`

```java
public static class RegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}
```

**Usage:** Transfers data between application layers while providing input validation and hiding internal domain model structure.

## 2. JEE - Java Enterprise Edition

### 2.1 Spring Boot Application Configuration
**Location:** `api/src/main/java/com/example/api/BinomeMatcherApiApplication.java`

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

**Usage:** Configures the Spring Boot application with component scanning, JPA entity scanning, and repository activation across multiple modules.

### 2.2 RESTful Web Services
**Location:** `api/src/main/java/com/example/api/controller/MatchController.java`

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
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(potentialMatches);
    }
}
```

**Usage:** Exposes HTTP endpoints for client consumption, handling match retrieval with proper HTTP status codes and JSON responses.

### 2.3 JPA Entity Mapping
**Location:** `core/src/main/java/com/example/core/domain/Profile.java`

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

**Usage:** Maps Java objects to database tables with relationships, enabling object-relational persistence for user profiles and skills.

### 2.4 Security Configuration with JWT
**Location:** `security/src/main/java/com/example/security/jwt/JwtTokenProvider.java`

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

**Usage:** Implements stateless authentication using JWT tokens, providing secure user session management without server-side storage.

### 2.5 Dependency Injection
**Location:** `api/src/main/java/com/example/api/service/AuthenticationService.java`

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

**Usage:** Injects dependencies through constructor injection, promoting loose coupling and facilitating unit testing.

## 3. Mobile Development - Android

### 3.1 Activity Lifecycle Management
**Location:** `client/app/src/main/java/com/example/client/MainActivity.java`

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

**Usage:** Manages Android activity lifecycle, initializes core components, and sets up navigation drawer for the main user interface.

### 3.2 HTTP Client with Authentication Interceptor
**Location:** `client/app/src/main/java/com/example/client/api/ApiClient.java`

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

**Usage:** Automatically adds authentication headers to HTTP requests, implementing the Chain of Responsibility pattern for request processing.

### 3.3 Shared Preferences for Data Persistence
**Location:** `client/app/src/main/java/com/example/client/api/ApiClient.java`

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

**Usage:** Persists user authentication tokens locally on the device, enabling session management across app restarts.

### 3.4 Navigation Component Integration
**Location:** `client/app/src/main/java/com/example/client/MainActivity.java`

```java
mAppBarConfiguration = new AppBarConfiguration.Builder(
        R.id.nav_dashboard, R.id.nav_profile, R.id.nav_matches, R.id.nav_messages)
        .setOpenableLayout(drawer)
        .build();

NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
NavigationUI.setupWithNavController(navigationView, navController);
```

**Usage:** Implements Android Navigation Component for seamless fragment navigation with drawer layout integration.

## Technical Architecture Summary

### Multi-Module Structure
- **Core Module:** Domain entities and shared business logic
- **API Module:** REST controllers and business services
- **Security Module:** Authentication and authorization components
- **Client Module:** Android application with UI components

### Key Technologies Integration
- **Spring Boot:** Enterprise application framework
- **Spring Security:** Authentication and authorization
- **JPA/Hibernate:** Object-relational mapping
- **JWT:** Stateless authentication tokens
- **Android Navigation:** Modern Android UI navigation
- **Retrofit/OkHttp:** HTTP client for API communication
- **SharedPreferences:** Local data persistence

This architecture demonstrates the integration of enterprise Java development with modern mobile application development, following established design patterns and best practices.