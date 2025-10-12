# BlogHub - Android Blogging Application 📱✍️

A modern, feature-rich Android blogging application built with Jetpack Compose, Firebase, and Material Design 3.

---

## 📋 Table of Contents

1. [Technologies & Frameworks](#technologies--frameworks)
2. [Project Structure & Files](#project-structure--files)
3. [Backend Architecture](#backend-architecture)
4. [Database](#database)
5. [Firebase Integration](#firebase-integration)
6. [Key Features](#key-features)
7. [Setup Instructions](#setup-instructions)

---

## 🛠️ Technologies & Frameworks

### **Core Technologies**

| Technology | Version | Purpose |
|------------|---------|---------|
| **Kotlin** | Latest | Primary programming language |
| **Jetpack Compose** | Latest | Modern UI toolkit (100% Compose, No XML) |
| **Material Design 3** | Latest | UI components and theming |
| **Firebase** | Latest | Backend services |
| **Cloudinary** | Latest | Image hosting and management |

### **Android Jetpack Components**

- **Compose UI** - Declarative UI framework
- **Navigation Compose** - Screen navigation
- **ViewModel** - UI state management
- **LiveData/StateFlow** - Reactive data streams
- **Lifecycle** - Lifecycle-aware components

### **Firebase Services**

- **Firebase Authentication** - User authentication (Email/Password)
- **Cloud Firestore** - NoSQL cloud database
- **Firebase Storage** - (Optional) File storage

### **Third-Party Libraries**

| Library | Purpose |
|---------|---------|
| **Coil** | Image loading and caching |
| **Cloudinary SDK** | Image upload and management |
| **Material Icons** | Icon library |
| **Kotlin Coroutines** | Asynchronous programming |

### **Additional Features & Permissions**

- **Camera Integration** - Take photos for profile pictures
- **Gallery Access** - Choose images from device
- **Internet Permission** - Network requests
- **File Provider** - Secure file sharing

### **UI Framework**

✅ **100% Jetpack Compose** (No XML layouts)
- Modern declarative UI
- Material 3 components
- Custom theming with light/dark mode
- Responsive layouts

---

## 📁 Project Structure & Files

### **1. Data Models** (`data/model/`)

#### **BlogModel.kt**
- **Purpose:** Defines the blog post data structure
- **Contains:** Post title, content, author info, category, likes, timestamps
- **Key Features:** Like functionality, category enum, author details

#### **UserModel.kt**
- **Purpose:** User profile data structure
- **Contains:** User ID, name, email, bio, profile image URL
- **Used For:** User authentication and profile management

#### **NotificationModel.kt**
- **Purpose:** In-app notification data structure
- **Contains:** Notification type, recipient, actor, post info, read status
- **Types:** LIKE, COMMENT, FOLLOW, MENTION

#### **Category.kt**
- **Purpose:** Blog category enumeration
- **Categories:** Technology, Lifestyle, Travel, Food, Health, Business, Education, Entertainment, Sports, Other

### **2. Repositories** (`data/model/`)

#### **AuthRepository.kt**
- **Purpose:** Handles all authentication operations
- **Functions:**
  - `signUp()` - Create new user account
  - `signIn()` - User login
  - `signOut()` - User logout
  - `getCurrentUser()` - Get current user
  - `getUserProfile()` - Fetch user profile from Firestore
  - `updateUserProfile()` - Update user profile
  - `uploadProfileImage()` - Upload profile picture to Cloudinary

#### **BlogRepository.kt**
- **Purpose:** Manages all blog post operations
- **Functions:**
  - `createPost()` - Create new blog post
  - `updatePost()` - Update existing post
  - `deletePost()` - Delete blog post
  - `getAllPosts()` - Fetch all posts (real-time)
  - `getUserPosts()` - Fetch user's posts
  - `getPostById()` - Get single post details
  - `toggleLike()` - Like/unlike post
  - `uploadImage()` - Upload blog images to Cloudinary

#### **NotificationRepository.kt**
- **Purpose:** Manages in-app notifications
- **Functions:**
  - `createNotification()` - Create new notification
  - `getUserNotifications()` - Fetch user notifications (real-time)
  - `markAsRead()` - Mark notification as read
  - `markAllAsRead()` - Mark all as read
  - `deleteNotification()` - Delete notification
  - `getUnreadCount()` - Get unread notification count

### **3. ViewModels** (`viewmodel/`)

#### **AuthViewModel.kt**
- **Purpose:** Manages authentication UI state
- **State:** Current user, loading, errors
- **Functions:** Sign up, sign in, sign out

#### **BlogViewModel.kt**
- **Purpose:** Manages blog posts UI state
- **State:** All posts, user posts, selected post, loading, errors
- **Functions:** Create, update, delete, like posts
- **Special:** Triggers notification creation on like

#### **ProfileViewModel.kt**
- **Purpose:** Manages user profile UI state
- **State:** User profile, loading, save status, errors
- **Functions:** Load profile, update profile, upload image

#### **NotificationViewModel.kt**
- **Purpose:** Manages notifications UI state
- **State:** Notifications list, unread count, loading, errors
- **Functions:** Load notifications, mark as read, delete

### **4. UI Screens** (`ui/screens/`)

#### **LoginScreen.kt**
- **Purpose:** User login interface
- **Features:** Email/password input, validation, error handling
- **Navigation:** Sign up, Home (on success)

#### **SignUpScreen.kt**
- **Purpose:** New user registration
- **Features:** Name, email, password, bio input, validation
- **Navigation:** Login, Home (on success)

#### **HomeScreen.kt**
- **Purpose:** Main feed showing all blog posts
- **Features:**
  - Display all blog posts
  - Like/unlike posts
  - Navigation drawer
  - Notification bell with badge
  - Search/filter (future)
- **Background:** Light blue (#F5F7FF) in light mode, dark (#0F1419) in dark mode

#### **MyBlogsScreen.kt**
- **Purpose:** Display user's own blog posts
- **Features:** Edit, delete own posts
- **Navigation:** Edit post, Blog detail

#### **AddEditBlogScreen.kt**
- **Purpose:** Create new or edit existing blog post
- **Features:**
  - Title, content, category input
  - Image upload (Cloudinary)
  - Image preview
  - Validation
- **Modes:** Create (new post) or Edit (existing post)

#### **BlogDetailScreen.kt**
- **Purpose:** Display full blog post details
- **Features:**
  - Full content view
  - Author information
  - Like functionality
  - Edit/delete (if owner)
  - Comments (future)

#### **ProfileScreen.kt**
- **Purpose:** Display user profile
- **Features:**
  - Profile picture
  - User info (name, email, bio)
  - Edit profile button
  - Logout

#### **EditProfileScreen.kt**
- **Purpose:** Edit user profile
- **Features:**
  - Update name, bio
  - Change profile picture (Camera or Gallery)
  - Bottom sheet for photo options
  - Image upload to Cloudinary

#### **NotificationScreen.kt**
- **Purpose:** Display in-app notifications
- **Features:**
  - List of notifications
  - Unread indicators (blue dot)
  - Mark as read on tap
  - Mark all as read
  - Navigate to post from notification
  - Time ago formatting
- **UI:** Light lavender background (#F8F9FF), beautiful cards

### **5. UI Components** (`ui/components/`)

#### **BlogCard.kt**
- **Purpose:** Reusable blog post card component
- **Features:**
  - Post image, title, author, content preview
  - Category chip
  - Like button with count
  - Rounded corners (16dp)
  - Adaptive colors (light/dark mode)

#### **RoundedButton.kt**
- **Purpose:** Reusable custom button component
- **Features:** Rounded corners, custom styling

### **6. Navigation** (`ui/`)

#### **Routes.kt**
- **Purpose:** Define all navigation routes
- **Routes:** Login, SignUp, Home, Profile, EditProfile, MyBlogs, AddBlog, BlogDetail, Notifications

#### **AppNavigation.kt**
- **Purpose:** Navigation graph and routing logic
- **Features:**
  - Authentication flow
  - Main app flow
  - Profile flow
  - Shared ViewModels across screens
  - Deep linking support

### **7. Theme** (`ui/theme/`)

#### **Color.kt**
- **Purpose:** Define app color palette
- **Light Theme Colors:**
  - Primary: #6E8CFB (Beautiful light blue)
  - Background: #F5F7FF (Soft blue-white)
  - Surface: #FFFFFF (White cards)
- **Dark Theme Colors:**
  - Primary: #8BA3FF (Bright blue)
  - Background: #0F1419 (Deep dark)
  - Surface: #1A1F29 (Dark cards)

#### **Theme.kt**
- **Purpose:** Material 3 theme configuration
- **Features:**
  - Light/dark mode support
  - Automatic theme detection
  - Custom color schemes
  - Status bar styling

#### **Type.kt**
- **Purpose:** Typography definitions
- **Contains:** Font styles, sizes, weights

### **8. Application Class**

#### **BlogHubApplication.kt**
- **Purpose:** Application initialization
- **Initializes:** Firebase, Cloudinary

### **9. Main Activity**

#### **MainActivity.kt**
- **Purpose:** Single activity host
- **Contains:** Compose UI setup, theme application

### **10. Configuration Files**

#### **AndroidManifest.xml**
- **Purpose:** App configuration and permissions
- **Permissions:**
  - INTERNET
  - CAMERA
  - READ_EXTERNAL_STORAGE
  - READ_MEDIA_IMAGES
- **Components:**
  - MainActivity
  - FileProvider (for camera photos)

#### **build.gradle (app)**
- **Purpose:** App-level dependencies and configuration
- **Dependencies:**
  - Compose libraries
  - Firebase SDK
  - Cloudinary SDK
  - Coil image loader
  - Navigation Compose

#### **build.gradle (project)**
- **Purpose:** Project-level configuration
- **Plugins:** Android, Kotlin, Google Services

#### **google-services.json**
- **Purpose:** Firebase configuration
- **Contains:** Firebase project credentials

#### **file_paths.xml**
- **Purpose:** FileProvider paths for camera photos
- **Location:** `res/xml/file_paths.xml`

#### **firestore.indexes.json**
- **Purpose:** Firestore composite indexes
- **Indexes:**
  - Blogs: `author.uid` + `createdAt`
  - Notifications: `recipientUserId` + `createdAt`

---

## 🔧 Backend Architecture

### **Backend Type: Firebase (Backend-as-a-Service)**

**Why Firebase?**
- ✅ No custom server needed
- ✅ Real-time data synchronization
- ✅ Built-in authentication
- ✅ Scalable cloud infrastructure
- ✅ Offline support
- ✅ Easy integration

**Architecture Pattern: MVVM (Model-View-ViewModel)**

```
┌─────────────────────────────────────────┐
│              UI Layer (Compose)          │
│  (Screens, Components, Navigation)       │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         ViewModel Layer                  │
│  (AuthViewModel, BlogViewModel, etc.)    │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│        Repository Layer                  │
│  (AuthRepository, BlogRepository, etc.)  │
└──────────────┬──────────────────────────┘
               │
               ↓
┌─────────────────────────────────────────┐
│         Data Sources                     │
│  (Firebase, Cloudinary)                  │
└─────────────────────────────────────────┘
```

**Data Flow:**
1. **UI** triggers action (e.g., create post)
2. **ViewModel** receives action, updates state
3. **Repository** performs Firebase operation
4. **Firebase** stores/retrieves data
5. **Repository** returns result
6. **ViewModel** updates UI state
7. **UI** recomposes with new data

---

## 💾 Database

### **Database Type: Cloud Firestore (NoSQL)**

**Why Firestore?**
- ✅ Real-time synchronization
- ✅ Offline support
- ✅ Scalable and flexible
- ✅ No SQL queries needed
- ✅ Automatic data syncing
- ✅ Built-in security rules

### **Database Structure**

```
Firestore Database
│
├── users/
│   └── {userId}/
│       ├── uid: String
│       ├── name: String
│       ├── email: String
│       ├── bio: String
│       ├── profileImageUrl: String
│       └── createdAt: Timestamp
│
├── blogs/
│   └── {blogId}/
│       ├── id: String
│       ├── title: String
│       ├── content: String
│       ├── imageUrl: String
│       ├── category: String
│       ├── author: Map {
│       │   ├── uid: String
│       │   ├── name: String
│       │   └── profileImageUrl: String
│       │   }
│       ├── likedBy: Array[String]
│       ├── likeCount: Number
│       ├── createdAt: Timestamp
│       └── updatedAt: Timestamp
│
└── notifications/
    └── {notificationId}/
        ├── id: String
        ├── type: String (LIKE, COMMENT, etc.)
        ├── recipientUserId: String
        ├── actorUserId: String
        ├── actorName: String
        ├── actorProfileImage: String
        ├── postId: String
        ├── postTitle: String
        ├── message: String
        ├── isRead: Boolean
        └── createdAt: Timestamp
```

### **Collections**

1. **users** - User profiles
2. **blogs** - Blog posts
3. **notifications** - In-app notifications

### **Indexes**

**Composite Indexes Required:**
- `blogs`: `author.uid` (Ascending) + `createdAt` (Descending)
- `notifications`: `recipientUserId` (Ascending) + `createdAt` (Descending)

### **Real-time Listeners**

- **Home Screen:** Listens to all blog posts
- **My Blogs:** Listens to user's posts
- **Notifications:** Listens to user's notifications

---

## 🔥 Firebase Integration

### **1. Firebase Authentication**

**Setup:**
```kotlin
// Initialize Firebase Auth
private val auth = FirebaseAuth.getInstance()
```

**Authentication Methods:**
- ✅ Email/Password authentication
- ✅ User session management
- ✅ Automatic login persistence

**Key Functions:**
```kotlin
// Sign Up
auth.createUserWithEmailAndPassword(email, password)

// Sign In
auth.signInWithEmailAndPassword(email, password)

// Sign Out
auth.signOut()

// Get Current User
auth.currentUser
```

**User Flow:**
1. User enters email/password
2. Firebase validates credentials
3. Firebase creates/authenticates user
4. User profile created in Firestore
5. User redirected to Home screen

### **2. Cloud Firestore**

**Setup:**
```kotlin
// Initialize Firestore
private val firestore = FirebaseFirestore.getInstance()
```

**Collections:**
```kotlin
private val usersCollection = firestore.collection("users")
private val blogsCollection = firestore.collection("blogs")
private val notificationsCollection = firestore.collection("notifications")
```

**CRUD Operations:**

**Create:**
```kotlin
blogsCollection.document(docId).set(blogModel)
```

**Read (Real-time):**
```kotlin
blogsCollection.addSnapshotListener { snapshot, error ->
    // Real-time updates
}
```

**Update:**
```kotlin
blogsCollection.document(docId).update(mapOf("title" to newTitle))
```

**Delete:**
```kotlin
blogsCollection.document(docId).delete()
```

**Queries:**
```kotlin
// Get user's posts
blogsCollection
    .whereEqualTo("author.uid", userId)
    .orderBy("createdAt", Query.Direction.DESCENDING)

// Get user's notifications
notificationsCollection
    .whereEqualTo("recipientUserId", userId)
    .orderBy("createdAt", Query.Direction.DESCENDING)
```

### **3. Firebase Configuration**

**google-services.json:**
- Located in `app/` directory
- Contains Firebase project credentials
- Auto-generated from Firebase Console

**Security Rules:**
```javascript
// Firestore Security Rules (set in Firebase Console)
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read all, write own
    match /users/{userId} {
      allow read: if true;
      allow write: if request.auth.uid == userId;
    }
    
    // Blogs can be read by all, written by authenticated users
    match /blogs/{blogId} {
      allow read: if true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.author.uid;
    }
    
    // Notifications can only be read/written by recipient
    match /notifications/{notificationId} {
      allow read, write: if request.auth.uid == resource.data.recipientUserId;
    }
  }
}
```

### **4. Cloudinary Integration**

**Purpose:** Image hosting and management

**Setup:**
```kotlin
// Initialize Cloudinary
MediaManager.init(
    context,
    mapOf(
        "cloud_name" to "YOUR_CLOUD_NAME",
        "api_key" to "YOUR_API_KEY",
        "api_secret" to "YOUR_API_SECRET"
    )
)
```

**Upload Flow:**
1. User selects/captures image
2. Image uploaded to Cloudinary
3. Cloudinary returns image URL
4. URL saved in Firestore

**Used For:**
- Profile pictures
- Blog post images
- Automatic image optimization

---

## ✨ Key Features

### **1. User Authentication**
- ✅ Email/Password sign up
- ✅ Secure login
- ✅ Session persistence
- ✅ Profile management

### **2. Blog Management**
- ✅ Create blog posts with images
- ✅ Edit own posts
- ✅ Delete own posts
- ✅ Category selection
- ✅ Rich text content

### **3. Social Features**
- ✅ Like/unlike posts
- ✅ View all posts feed
- ✅ Author profiles
- ✅ Real-time updates

### **4. Notifications**
- ✅ In-app notifications
- ✅ Like notifications
- ✅ Unread badge count
- ✅ Mark as read
- ✅ Navigate to post from notification
- ✅ Real-time notification updates

### **5. Profile Management**
- ✅ View profile
- ✅ Edit profile (name, bio)
- ✅ Change profile picture
- ✅ Camera integration
- ✅ Gallery picker

### **6. Camera Integration**
- ✅ Take photo with camera
- ✅ Choose from gallery
- ✅ Bottom sheet UI
- ✅ Permission handling
- ✅ Full quality photos

### **7. Modern UI/UX**
- ✅ Material Design 3
- ✅ Light/Dark mode
- ✅ Beautiful color scheme (blue/purple gradient)
- ✅ Smooth animations
- ✅ Responsive layouts
- ✅ Custom app logo

### **8. Real-time Features**
- ✅ Live blog feed updates
- ✅ Real-time like counts
- ✅ Instant notifications
- ✅ Live notification badge

### **9. Image Management**
- ✅ Cloudinary integration
- ✅ Image upload
- ✅ Image optimization
- ✅ Image preview

### **10. Navigation**
- ✅ Bottom navigation
- ✅ Navigation drawer
- ✅ Deep linking support
- ✅ Back stack management

---

## 🎨 UI/UX Features

### **Color Scheme**

**Light Mode:**
- Background: #F5F7FF (Soft blue-white)
- Cards: #FFFFFF (Pure white)
- Primary: #6E8CFB (Light blue)
- Accent: #9B7EF8 (Soft purple)

**Dark Mode:**
- Background: #0F1419 (Deep dark)
- Cards: #1A1F29 (Dark blue-gray)
- Primary: #8BA3FF (Bright blue)
- Accent: #B39DFF (Soft purple)

### **Design Elements**
- ✅ Rounded corners (16dp)
- ✅ Elevated cards (4dp)
- ✅ Smooth gradients
- ✅ Modern typography
- ✅ Intuitive icons

---

## 📱 App Flow

### **Authentication Flow**
```
Launch App
    ↓
Check if logged in?
    ├─ Yes → Home Screen
    └─ No → Login Screen
              ↓
         Login/Sign Up
              ↓
         Home Screen
```

### **Blog Creation Flow**
```
Home Screen
    ↓
Tap FAB (+)
    ↓
Add Blog Screen
    ↓
Enter title, content, category
    ↓
Upload image (optional)
    ↓
Tap Save
    ↓
Upload to Cloudinary
    ↓
Save to Firestore
    ↓
Navigate back to Home
    ↓
New post appears in feed
```

### **Like & Notification Flow**
```
User A creates post
    ↓
User B sees post in feed
    ↓
User B taps like ♥
    ↓
Post likeCount increases
    ↓
Notification created in Firestore
    ↓
User A's app receives notification
    ↓
Badge appears on bell icon
    ↓
User A taps bell
    ↓
Notification screen opens
    ↓
User A sees "User B liked your post"
    ↓
User A taps notification
    ↓
Post detail screen opens
```

---

## 🔒 Security

### **Authentication**
- ✅ Firebase Authentication
- ✅ Secure password storage
- ✅ Email verification (optional)

### **Data Security**
- ✅ Firestore Security Rules
- ✅ User can only edit/delete own posts
- ✅ User can only edit own profile
- ✅ Notifications only visible to recipient

### **API Keys**
- ✅ Cloudinary credentials in code (should be moved to BuildConfig)
- ✅ Firebase credentials in google-services.json

---

## 🚀 Setup Instructions

### **Prerequisites**
- Android Studio (Latest version)
- Android SDK (API 21+)
- Firebase account
- Cloudinary account

### **Installation Steps**

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd "Android Project"
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - File → Open → Select project folder

3. **Firebase Setup**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Create new project or use existing
   - Add Android app
   - Download `google-services.json`
   - Place in `app/` directory
   - Enable Authentication (Email/Password)
   - Create Firestore database

4. **Cloudinary Setup**
   - Go to [Cloudinary](https://cloudinary.com/)
   - Create account
   - Get credentials (cloud_name, api_key, api_secret)
   - Update in `BlogHubApplication.kt`

5. **Firestore Indexes**
   - Go to Firebase Console → Firestore → Indexes
   - Create composite indexes:
     - `blogs`: `author.uid` (Ascending) + `createdAt` (Descending)
     - `notifications`: `recipientUserId` (Ascending) + `createdAt` (Descending)
   - OR use the error links when running the app

6. **Build and Run**
   - Sync Gradle files
   - Build → Rebuild Project
   - Run on device/emulator

---

### **Key Points to Mention**

- ✅ 100% Jetpack Compose (modern UI)
- ✅ Material Design 3 (latest design system)
- ✅ MVVM architecture (clean code)
- ✅ Firebase backend (scalable)
- ✅ Real-time updates (instant sync)
- ✅ Light/Dark mode (accessibility)
- ✅ Camera integration (native features)
- ✅ In-app notifications (user engagement)

---

## 📈 Future Enhancements

- [ ] Comments on posts
- [ ] Follow/Unfollow users
- [ ] Search functionality
- [ ] Post categories filter
- [ ] Share posts
- [ ] Bookmark posts
- [ ] Rich text editor
- [ ] Video support
- [ ] Push notifications (FCM)
- [ ] Analytics

---

## 📄 License

This project is created for educational purposes.

---
**Built with ❤️ using Kotlin, Jetpack Compose, and Firebase**
