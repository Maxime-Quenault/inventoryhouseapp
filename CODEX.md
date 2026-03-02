🧠 Project Overview

Inventory House est une application mobile Android permettant de gérer l’inventaire alimentaire d’un foyer (frigo, congélateur, placard).

L’application permet :

📦 Ajouter des produits (scan ou manuel)

🏠 Gérer un stock par maison

👥 Authentification utilisateur

☁️ Synchronisation avec un backend Node.js + PostgreSQL

🗄️ Persistance locale via Room

🔐 Gestion de session via JWT

📱 Mobile Application
🔧 Stack Technique

Language: Kotlin

UI: Jetpack Compose (Material 3)

Architecture: MVVM + Repository Pattern

State Management: StateFlow

Navigation: Root-based navigation (RootDestination enum)

Local DB: Room

Networking: Retrofit + Gson + OkHttp

Token Storage: DataStore (Preferences)

Barcode Scanning: ML Kit (Google Play Services)

🏗 Architecture Mobile
Layering
ui/
screen/
auth/
login/
register/
main/
viewmodel/
domain/
model/
repository/
data/
remote/
api/
dto/
network/
local/
entity/
dao/
session/
repository/
🧩 UI Layer

Chaque écran respecte :

Screen (Composable)
↓
ViewModel
↓
State (StateFlow)

Pattern utilisé :

LoginState

LoginEvent

LoginViewModel

LoginScreen

Les screens sont "bêtes" :

affichent state

envoient des events

aucune logique métier

🧠 ViewModel

Expose un StateFlow<State>

Appelle les repositories

Gère loading / error

Peut déclencher navigation via callback

📦 Repository Pattern

Interfaces dans domain/repository

Implémentations dans data/repository

Exemple :

AuthRepository
ProductRepository

Permet :

découplage Room / API

testabilité

évolutivité

🔐 Authentication Flow
Backend

Routes :

POST /api/auth/register
POST /api/auth/login
Register Body
{
"email": "...",
"password": "...",
"confirmPassword": "..."
}
Login Body
{
"email": "...",
"password": "..."
}
Success Response
{
"user": {
"id": 1,
"email": "..."
},
"token": "JWT_TOKEN"
}
Error Response
{
"error": "Message"
}
Mobile Auth Flow

User clicks "Se connecter"

LoginEvent.Submit

ViewModel calls AuthRepository.login()

API call via Retrofit

JWT token stored in DataStore

RootDestination → MAIN

🗄️ Local Storage
Room Database

ProductEntity

ProductDao

AppDatabase

Uses Flow:

@Query("SELECT * FROM products")
fun getAll(): Flow<List<ProductEntity>>

ViewModel exposes:

StateFlow<StockState>
Session Management

DataStore:

Stores auth_token

Stores token_saved_at

Future plan:

Add OkHttp Interceptor for Authorization: Bearer <token>

🌐 Backend
Stack

Node.js

Express

PostgreSQL

Sequelize

JWT

bcrypt

Database Structure (Current)
Users
id
email
name
password_hash
Houses (planned / active)
id
name
Products
id
house_id
name
expiration_date
location
🧭 Root Navigation System

Enum:

RootDestination {
ONBOARDING,
LOGIN,
REGISTER,
MAIN
}

Flow:

App launch

Check onboarding completed (DataStore)

If not → ONBOARDING

Else → LOGIN or MAIN (depending on token presence)

🎯 Project Objectives

Short-term:

Complete authentication flow

Add Authorization header interceptor

Secure product routes

Link products to authenticated house

Mid-term:

Multi-user per house

Invite system

Auto logout on token expiry

Proper error handling UI

Long-term:

Production-ready architecture

Clean Architecture separation (Domain/Data/UI modules)

Hilt for DI

Offline-first sync strategy

⚠️ Important Implementation Notes

Emulate backend via http://10.0.2.2:<port>/

android:usesCleartextTraffic="true" enabled in debug

Retrofit instance created via ApiClient

ViewModels instantiated with custom ViewModelFactory

No Hilt yet (manual DI)

🔮 Planned Improvements

OkHttp Interceptor for token injection

Refresh token mechanism

Centralized error handling

Sealed Result wrapper

Navigation Compose instead of manual Root enum

MVI architecture (possible future refactor)

🧑‍💻 Developer Context

Background: Java developer

Strong understanding of DTO, Model, layered architecture

Learning Kotlin + Compose

Goal: build a clean full-stack project (Android + Node + PostgreSQL)

📌 Current State of Project

✔ Room implemented
✔ Flow + StateFlow reactive architecture
✔ Repository pattern
✔ Backend operational
✔ Auth API connected
✔ Login screen MVVM structured