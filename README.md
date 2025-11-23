Digital Logistics – Security Documentation
Table des matières

Sécurité Web & Concepts de Base

Architecture Spring Security

Flux d’authentification JWT

Basic Auth – Fonctionnement & Flux

Endpoints sécurisés et rôles

Exemples de tests Postman / cURL

Composants Spring Security

Bonnes pratiques

A – Sécurité Web & Concepts de Base
1. Authentification vs Autorisation

Authentification : vérifier l’identité de l’utilisateur (who you are).

Autorisation : vérifier les permissions et le rôle (what you can access).

2. Attaques web courantes

Brute force : tenter plusieurs combinaisons username/password.

XSS : injection de scripts malveillants dans le site.

CSRF : requête malveillante exploitant les cookies valides.

Session Fixation : hacker force un utilisateur à utiliser une session existante.

Vol de session : hacker vole un sessionId.

3. Importance de HTTPS

Chiffre la communication client ↔ serveur.

Protège les mots de passe, cookies, et tokens (JWT/Basic Auth).

4. Défense en profondeur (Defense in Depth)

Plusieurs couches de sécurité pour limiter les risques et protéger l’application.

B – Architecture Spring Security
Composants principaux
Composant	Rôle
SecurityFilterChain	Définit les règles et filtres HTTP
DelegatingFilterProxy	Redirige les requêtes vers Spring Security
AuthenticationManager	Gère l’authentification complète
AuthenticationProvider	Valide les credentials
UserDetailsService	Charge les utilisateurs depuis la DB
PasswordEncoder	Hache les mots de passe (BCrypt)
Roles vs Authorities	Role = catégorie, Authority = permission exacte
Schéma flux requête sécurisée
Client -> DelegatingFilterProxy -> SecurityFilterChain -> JwtAuthFilter (si JWT)
-> DaoAuthenticationProvider (si Basic Auth)
-> Vérification roles/authorities
-> Contrôleur REST
-> Réponse

C – Flux d’authentification JWT

Client envoie la requête avec Authorization: Bearer <token>.

JwtAuthFilter vérifie le token et charge les UserDetails.

SecurityContext est rempli si le token est valide.

AuthorizationManager vérifie les rôles (hasRole("ADMIN")).

Contrôleur REST exécute l’endpoint.

Réponse envoyée au client.

Schéma ASCII :

Client --> [Header JWT]
--> SecurityFilterChain (jwtSecurityChain)
--> JwtAuthFilter
--> UserDetailsService
--> DB (user table)
--> Validate JWT
--> AuthorizationManager (roles)
--> Contrôleur REST
--> Réponse

D – Basic Auth – Fonctionnement & Flux
1. Définition

Le client envoie username et password encodés en Base64 dans le header Authorization.

2. Format
   Authorization: Basic base64(username:password)

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

À utiliser uniquement via HTTPS.

Stateless (pas de session côté serveur).

Moins flexible que JWT.

E – Endpoints sécurisés et rôles
Endpoint	Méthode	Auth	Rôle requis	Exemple
/basic/users/by-email	GET	Basic Auth	ADMIN, WAREHOUSE_MANAGER	/basic/users/by-email?email=admin@logistics.com
/basic/users	POST	Basic Auth	ADMIN	JSON body avec username/password
/api/auth/login	POST	JWT	N/A	JSON body {email, password}
/api/products	GET	JWT	AUTHENTICATED	Header JWT
/api/inventory	GET	JWT	AUTHENTICATED	Header JWT
/api/orders	POST	JWT	AUTHENTICATED	Header JWT + JSON body
/api/shipments	POST	JWT	AUTHENTICATED	Header JWT
/api/admin/**	GET/POST	JWT	ADMIN	Header JWT
F – Exemples de tests Postman / cURL
Basic Auth GET
curl -u admin@logistics.com:123456 http://localhost:8083/basic/users/by-email?email=admin@logistics.com

JWT POST login
curl -X POST http://localhost:8083/api/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"user@logistics.com","password":"123456"}'

JWT GET request
curl -H "Authorization: Bearer <jwt_token>" http://localhost:8083/api/products

G – Composants Spring Security
Composant	Rôle
SecurityFilterChain	Sépare les flux JWT et Basic Auth via @Order
AuthenticationProvider	Vérifie credentials via UserDetailsService
MyUserDetailsService	Charge utilisateurs depuis la DB
PasswordEncoder	Encode les mots de passe (BCrypt)
JwtAuthFilter	Valide le token JWT et remplit SecurityContext
AuthenticationManager	Valide les credentials pour JWT login
H – Bonnes pratiques

Utiliser BCrypt pour le hash des mots de passe.

JWT = stateless, Basic Auth = stateless → pas de session côté serveur.

Séparer les chaînes de filtres JWT et Basic Auth (@Order).

Définir correctement les rôles et authorities dans la DB.

Ne jamais stocker de mot de passe en clair.

Rotation régulière des mots de passe et politique de sécurité.