# Contexte pour future session Codex

Projet Android: `C:\Users\quena\AndroidStudioProjects\InventoryHouse`

## Objectif produit

L'application mobile InventoryHouse doit permettre le scenario simple suivant:

1. Garder un onboarding au demarrage.
2. Se connecter avec email + mot de passe ou avec Google.
3. Creer sa maison si l'utilisateur n'en a pas encore.
4. Ajouter des membres existants a la maison.
5. Gerer le stock de la maison.
6. Scanner de nouveaux items pour les ajouter au stock.

## Backend

Swagger fourni: `C:\Users\quena\Downloads\api-docs.json`

Backend heberge sur Vercel:

```text
https://inventoryhouseback.vercel.app/
```

Important pour Retrofit:

```kotlin
.baseUrl("https://inventoryhouseback.vercel.app/")
```

Ne pas mettre `/api` dans la base URL, car les interfaces Retrofit declarent deja les chemins comme `api/auth/login`, `api/houses`, etc.

## Endpoints principaux

Auth:

```text
POST /api/auth/register
POST /api/auth/login
POST /api/auth/google
GET  /api/auth/me
POST /api/auth/logout
```

Connexion Google:

```json
{
  "idToken": "string"
}
```

Reponse attendue:

```json
{
  "user": {
    "id": 1,
    "name": "Alex Martin",
    "email": "alex@example.com",
    "auth_provider": "local",
    "created_at": "2026-05-07T14:53:47.308Z",
    "updated_at": "2026-05-07T14:53:47.308Z"
  },
  "token": "string"
}
```

Erreur attendue:

```json
{
  "error": "Invalid credentials"
}
```

Maisons et membres:

```text
GET  /api/houses
POST /api/houses
GET  /api/houses/{houseId}/members
POST /api/houses/{houseId}/members
PATCH /api/houses/{houseId}/members/{userId}
DELETE /api/houses/{houseId}/members/{userId}
```

Stock:

```text
GET    /api/references
GET    /api/houses/{houseId}/items
POST   /api/houses/{houseId}/items
PATCH  /api/houses/{houseId}/items/{itemId}
DELETE /api/houses/{houseId}/items/{itemId}
GET    /api/houses/{houseId}/stock-movements
```

## Changements deja faits dans l'app

Couche API:

- Ajout de `InventoryApi.kt` pour les endpoints Swagger hors auth.
- Ajout de `InventoryDtos.kt` pour maisons, membres, references, items, mouvements.
- Ajout de `GoogleLoginRequestDto.kt`.
- `AuthApi.kt` contient maintenant `POST api/auth/google`.
- `ApiClient.kt` injecte le JWT depuis `SessionStore` via l'header `Authorization: Bearer <token>`.

Repositories:

- `InventoryRepository.kt` centralise les appels API et parse les erreurs `{ "error": "..." }`.
- `RemoteProductRepository.kt` connecte `ProductRepository` au backend distant par maison.
- `InMemoryProductRepository.kt` existe encore pour compatibilite locale, mais le flux principal utilise le remote.

Navigation et UI:

- `MainActivity.kt` gere le flux:
  - onboarding,
  - login/register,
  - main,
  - creation obligatoire d'une maison si aucune maison n'existe.
- Ajout de `ui/screen/house` pour creation maison et gestion membres.
- Ajout de `ui/screen/dashboard` pour dashboard connecte a la maison courante.
- Le stock n'affiche plus de faux items si l'API ne renvoie rien.
- Le scan CameraX/MLKit + OpenFoodFacts ajoute maintenant les produits via `RemoteProductRepository`.
- L'ecran settings contient la deconnexion stateless: suppression du token local.

Google Sign-In:

- Dependence ajoutee:

```kotlin
implementation("com.google.android.gms:play-services-auth:21.2.0")
```

- `build.gradle.kts` expose une ressource:

```kotlin
resValue(
    "string",
    "google_server_client_id",
    providers.gradleProperty("GOOGLE_WEB_CLIENT_ID").orElse("").get()
)
```

- `gradle.properties` contient une entree a completer:

```properties
GOOGLE_WEB_CLIENT_ID=
```

Il faut y mettre le client id OAuth Web Google, par exemple:

```properties
GOOGLE_WEB_CLIENT_ID=xxx.apps.googleusercontent.com
```

Sans ce client id, le bouton Google affiche une erreur claire.

## Point important Vercel

L'utilisateur a ouvert:

```text
https://inventoryhouseback.vercel.app/api/auth/google
```

dans un navigateur, ce qui fait un `GET`. L'endpoint attend un `POST`, donc ce n'est pas le bon test.

Cependant Vercel a affiche:

```text
500 INTERNAL_SERVER_ERROR
FUNCTION_INVOCATION_FAILED
```

Ce n'est pas ideal: meme sur une mauvaise methode ou un body manquant, le backend devrait repondre proprement avec `405`, `400` ou `401`, pas crasher.

Test conseille:

```bash
curl -i -X POST https://inventoryhouseback.vercel.app/api/auth/google ^
  -H "Content-Type: application/json" ^
  -d "{\"idToken\":\"test\"}"
```

Avec un faux token, le backend devrait renvoyer:

```json
{
  "error": "Invalid credentials"
}
```

et non un `500`.

Verifier cote backend Vercel:

- Logs Vercel du crash.
- Route qui gere explicitement les methodes non `POST`.
- Variable env Google configuree.
- Variables env DB/JWT configurees.
- Gestion du cas `idToken` absent sans exception non capturee.

## Verification locale

La compilation Android a ete lancee plusieurs fois:

```powershell
.\gradlew.bat :app:assembleDebug
```

Dernier resultat connu: `BUILD SUCCESSFUL`.

Warnings restants connus:

- GoogleSignIn est marque deprecated par Google Play Services, mais fonctionne encore pour ce flux.
- Room peut avertir sur l'export de schema si le module local Room est compile.

## Prochaine action probable

1. Remplacer dans `ApiClient.kt`:

```kotlin
.baseUrl("http://10.0.2.2:3000/")
```

par:

```kotlin
.baseUrl("https://inventoryhouseback.vercel.app/")
```

2. Completer `GOOGLE_WEB_CLIENT_ID` dans `gradle.properties`.
3. Tester le backend Vercel avec `curl` ou Postman.
4. Relancer:

```powershell
.\gradlew.bat :app:assembleDebug
```
