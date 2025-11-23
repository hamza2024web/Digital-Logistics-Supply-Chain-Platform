Digital Logistics – Security Documentation
A — Sécurité Web & Concepts de Base
1. Authentification vs Autorisation

Authentification : vérifier qui est l’utilisateur qui essaie de se connecter (verify who you are).

Autorisation : vérifier les permissions de l’utilisateur (son rôle et les endpoints qu’il peut accéder).

2. Attaques web courantes

Brute force : un hacker tente plusieurs combinaisons username/password aléatoires jusqu’à trouver la bonne.

XSS : injection de scripts JavaScript malveillants dans le site web.

CSRF : le victim envoie une requête inattendue avec des cookies valides.

Session Fixation : un hacker force un victim à utiliser un sessionId valide.

Vol de session : un attacker vole un sessionId valide pour prendre le contrôle de la session.

3. Importance de HTTPS

Chiffre la communication entre client et serveur, sécurisant authentification, sessions, cookies et API.

4. Principe “Defense in Depth”

Protéger l’application avec plusieurs couches de sécurité pour limiter les risques.

B — Architecture moderne de Spring Security
1. SecurityFilterChain

Bean Spring qui définit les règles et filtres pour chaque requête HTTP.

2. DelegatingFilterProxy

Point d’entrée qui redirige les requêtes du Servlet Container vers Spring Security (SecurityFilterChain).

3. AuthenticationManager

Composant responsable de la gestion complète du processus d’authentification.

4. AuthenticationProvider

Classe qui effectue une tâche spécifique : valider les credentials de login.

5. UserDetailsService

Charge les informations d’un utilisateur depuis la base de données.

6. PasswordEncoder

Encode/hache les mots de passe (ici BCrypt) avant de les stocker dans la base de données.

7. Différence Roles / Authorities

Role : catégorie d’utilisateur (ADMIN, WAREHOUSE_MANAGER, CLIENT)

Authority : permission exacte assignée à un utilisateur (READ_PRODUCTS, WRITE_ORDERS).

8. Disparition de WebSecurityConfigurerAdapter

Trop “magique” et incompatible avec l’injection de dépendances (IOC).

Spring Security 6+ utilise SecurityFilterChain et configuration par bean.

Schéma flux JWT
┌───────────────────────────────┐
│     1. Client envoie requête   │
│  GET /api/orders avec JWT      │
└───────────────┬──────────────┘
                │
                ▼
┌───────────────────────────┐
│ DelegatingFilterProxy     │
│ (vers SecurityFilterChain)│
└───────────────┬──────────┘
                │
                ▼
┌───────────────────────────┐
│   SecurityFilterChain     │
└───────────────┬──────────┘
                │
                ▼
┌───────────────────────────┐
│      JwtAuthFilter        │
│ - Vérifie header Bearer   │
│ - Extrait username        │
│ - Charge UserDetails      │
│ - Valide le token         │
│ - Remplit SecurityContext │
└───────────────┬──────────┘
                │ (si JWT valide)
                ▼
┌───────────────────────────┐
│ AuthorizationManager       │
│ Vérifie roles/authorities │
│ Exemple : hasRole("ADMIN")│
└───────────────┬──────────┘
                │
                ▼
┌───────────────────────────┐
│     Contrôleur REST       │
│   (exécute l’endpoint)    │
└───────────────┬──────────┘
                │
                ▼
┌───────────────────────────┐
│   Réponse envoyée au client │
└───────────────────────────┘

C — Basic Auth – Fonctionnement & Flux
1. Définition

Basic Auth est un mécanisme simple où le client envoie username et password encodés en Base64 dans le header Authorization.

2. Format du header
   Authorization: Basic base64(username:password)


Exemple :

username = admin@logistics.com
password = 123456
Header: Authorization: Basic YWRtaW5AbG9naXN0aWNzLmNvbToxMjM0NTY=

3. Flux Basic Auth
   Client --> [Authorization Header Basic Auth]
   --> SecurityFilterChain (basicAuthChain)
   --> DaoAuthenticationProvider
   --> MyUserDetailsService
   --> DB (user table)
   --> PasswordEncoder (BCrypt)
   --> Vérification des rôles (hasRole("ADMIN"))
   --> Accès autorisé ou 403 Forbidden

4. Limites

Utiliser uniquement via HTTPS

Stateless (pas de session persistante côté serveur)

Moins flexible que JWT pour des systèmes distribués

D — Endpoints sécurisés et rôles
Endpoint	Méthode	Auth	Rôle requis	Exemple
/basic/users/by-email	GET	Basic Auth	ADMIN, WAREHOUSE_MANAGER	/basic/users/by-email?email=admin@logistics.com
/basic/users	POST	Basic Auth	ADMIN	Body JSON avec username et password
/api/auth/login	POST	JWT	N/A	Body JSON avec email/password
/api/products	GET	JWT	AUTHENTICATED	Header JWT
/api/inventory	GET	JWT	AUTHENTICATED	Header JWT
/api/orders	POST	JWT	AUTHENTICATED	Header JWT + Body JSON
/api/shipments	POST	JWT	AUTHENTICATED	Header JWT
/api/admin/**	GET/POST	JWT	ADMIN	Header JWT
E — Exemples de tests Postman / cURL
Basic Auth GET
curl -u admin@logistics.com:123456 http://localhost:8083/basic/users/by-email?email=admin@logistics.com

JWT POST login
curl -X POST http://localhost:8083/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"user@logistics.com","password":"123456"}'

JWT GET request
curl -H "Authorization: Bearer <jwt_token>" http://localhost:8083/api/products

F — Composants Spring Security
Composant	Rôle
SecurityFilterChain	Filtres séparés pour JWT et Basic Auth avec @Order
AuthenticationProvider	Vérifie credentials via UserDetailsService
MyUserDetailsService	Charge les utilisateurs depuis la DB
PasswordEncoder	Encode le mot de passe avec BCrypt
JwtAuthFilter	Valide le token JWT et remplit le SecurityContext
AuthenticationManager	Valide les credentials pour JWT login
G — Bonnes pratiques

BCrypt pour le hash des mots de passe

JWT = stateless → pas de session côté serveur

Basic Auth → toujours via HTTPS

Séparer les chaînes de filtres pour JWT et Basic Auth (@Order)

Définir correctement les rôles et authorities dans la DB

Ne jamais stocker de mot de passe en clair

Rotation régulière des mots de passe et politiques de sécurité