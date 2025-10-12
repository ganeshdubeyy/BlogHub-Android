# Like Button Feature

## ✅ What Was Implemented

A fully functional **Like/Unlike system** for blog posts with real-time count updates and Firestore persistence. The bookmark feature has been removed as requested.

---

## 🎯 Features

### **Working Like Button**
- **Toggle Functionality** - Tap to like/unlike posts
- **Visual Feedback** 
  - Outlined heart icon when not liked
  - Filled red heart icon when liked
- **Real-time Count** - Shows number of likes
- **Optimistic Updates** - UI updates instantly, syncs with Firestore in background
- **User-specific State** - Each user's like status is tracked individually
- **Persistent Storage** - Likes are saved to Firestore

### **Removed Features**
- ❌ Bookmark button removed
- ❌ Bookmark count removed

---

## 📝 Files Modified

### **1. BlogRepository.kt (BlogModel + Repository)**
**Location:** `app/src/main/java/com/example/bloghub/data/model/BlogRepository.kt`

**BlogModel Changes:**
```kotlin
data class BlogModel(
    // ... existing fields ...
    val likedBy: List<String> = emptyList() // ✅ NEW: List of user IDs who liked
) {
    val likeCount: Int get() = likedBy.size  // ✅ Helper property
    fun isLikedBy(userId: String): Boolean   // ✅ Helper function
}
```

**Repository Functions Added:**
- ✅ `likePost(postId, userId)` - Adds user to likedBy list
- ✅ `unlikePost(postId, userId)` - Removes user from likedBy list
- ✅ `toggleLike(postId, userId, currentlyLiked)` - Convenience toggle function

**How it works:**
- Stores array of user IDs in Firestore: `likedBy: ["user1", "user2", "user3"]`
- Updates Firestore document by adding/removing user ID from array
- Prevents duplicate likes (checks if user already in list)

### **2. BlogViewModel.kt**
**Location:** `app/src/main/java/com/example/bloghub/viewmodel/BlogViewModel.kt`

**Function Added:**
```kotlin
fun toggleLike(postId: String)
```

**Features:**
- ✅ **Optimistic UI Update** - Updates UI immediately for instant feedback
- ✅ **Background Sync** - Updates Firestore in background
- ✅ **Error Handling** - Reverts UI if Firestore update fails
- ✅ **Auth Check** - Ensures user is logged in before allowing likes
- ✅ **Dual List Update** - Updates both `allPosts` and `myPosts` lists

**Flow:**
1. User taps like button
2. UI updates instantly (optimistic)
3. Firestore update happens in background
4. If Firestore fails, UI reverts to original state

### **3. BlogCard.kt**
**Location:** `app/src/main/java/com/example/bloghub/ui/components/BlogCard.kt`

**Changes:**
- ✅ Added `currentUserId` parameter
- ✅ Added `onLikeClick` callback parameter
- ✅ Updated like button to show filled/outlined heart based on like status
- ✅ Changed icon color to red when liked
- ✅ Display real like count from `post.likeCount`
- ❌ Removed bookmark icon button
- ❌ Removed bookmark count text

**Visual States:**
```
Not Liked: ♡ (outlined, default color) + count
Liked:     ♥ (filled, red color) + count (red)
```

### **4. HomeScreen.kt**
**Location:** `app/src/main/java/com/example/bloghub/ui/screens/HomeScreen.kt`

**Changes:**
- ✅ Added `onLikeClick` parameter to `BlogPostItem`
- ✅ Passed `currentUserId` to `BlogCard`
- ✅ Wired up `onLikeClick` to call `blogViewModel.toggleLike(post.id)`

### **5. MyBlogsScreen.kt**
**Location:** `app/src/main/java/com/example/bloghub/ui/screens/MyBlogsScreen.kt`

**Changes:**
- ✅ Added `currentUserId` parameter to screen
- ✅ Added `onLikeClick` parameter to `MyBlogPostItem`
- ✅ Passed `currentUserId` to `BlogCard`
- ✅ Wired up `onLikeClick` to call `blogViewModel.toggleLike(post.id)`

### **6. AppNavigation.kt**
**Location:** `app/src/main/java/com/example/bloghub/ui/AppNavigation.kt`

**Changes:**
- ✅ Updated `MyBlogsScreen` call to pass `currentUserId`

---

## 🎨 UI Design

### **BlogCard Layout (Updated)**
```
┌─────────────────────────────────────┐
│  [Featured Image]                   │
├─────────────────────────────────────┤
│  Title                              │
│  By Author Name                     │
│  Content preview...                 │
│                                     │
│  [General]          ♥ 5            │  ← Like button + count
└─────────────────────────────────────┘
```

### **Like Button States**

**State 1: Not Liked**
```
♡ 5
```
- Outlined heart icon
- Default text color
- Count shows total likes

**State 2: Liked by Current User**
```
♥ 6
```
- Filled heart icon (red)
- Red text color
- Count increased by 1

---

## 🔄 User Flow

### **Liking a Post**
1. User sees a blog post with ♡ icon and count (e.g., "5")
2. User taps the heart icon
3. ✨ **Instant feedback:** Icon changes to ♥ (red), count becomes "6"
4. 🔄 **Background:** Firestore updates `likedBy` array
5. ✅ **Success:** Change persists
6. ❌ **Failure:** Icon reverts to ♡, count back to "5", error message shown

### **Unliking a Post**
1. User sees a post they previously liked (♥ red icon)
2. User taps the heart icon again
3. ✨ **Instant feedback:** Icon changes to ♡ (outlined), count decreases
4. 🔄 **Background:** Firestore removes user from `likedBy` array
5. ✅ **Success:** Change persists
6. ❌ **Failure:** Icon reverts to ♥, count increases, error message shown

---

## 🧪 How to Test

### **Test Case 1: Like a Post**
1. ✅ Open app and go to Home screen
2. ✅ Find a post you haven't liked (outlined heart)
3. ✅ Note the current like count (e.g., "5")
4. ✅ Tap the heart icon
5. ✅ **Verify:** Icon immediately turns red and filled
6. ✅ **Verify:** Count increases to "6"
7. ✅ Refresh the screen (pull down or navigate away and back)
8. ✅ **Verify:** Like persists (still red heart, count still "6")

### **Test Case 2: Unlike a Post**
1. ✅ Find a post you've already liked (red filled heart)
2. ✅ Note the current like count
3. ✅ Tap the heart icon
4. ✅ **Verify:** Icon changes to outlined heart
5. ✅ **Verify:** Count decreases by 1
6. ✅ **Verify:** Color changes from red to default
7. ✅ Refresh the screen
8. ✅ **Verify:** Unlike persists

### **Test Case 3: Multiple Users Liking**
1. ✅ Like a post from User A's account
2. ✅ Note the count (e.g., "3")
3. ✅ Log out and log in as User B
4. ✅ **Verify:** Same post shows count "3" but outlined heart (User B hasn't liked)
5. ✅ Like the post as User B
6. ✅ **Verify:** Count increases to "4"
7. ✅ Log back in as User A
8. ✅ **Verify:** Post shows "4" likes with filled red heart (User A's like persists)

### **Test Case 4: Like on My Blogs Screen**
1. ✅ Navigate to "My Blogs" screen
2. ✅ Like/unlike your own posts
3. ✅ **Verify:** Functionality works identically to Home screen

### **Test Case 5: Like Persistence Across Screens**
1. ✅ Like a post on Home screen
2. ✅ Navigate to Blog Detail screen for that post
3. ✅ Go back to Home
4. ✅ **Verify:** Like status is preserved
5. ✅ Navigate to My Blogs (if it's your post)
6. ✅ **Verify:** Like status matches

### **Test Case 6: Offline Behavior**
1. ✅ Turn off internet connection
2. ✅ Try to like a post
3. ✅ **Verify:** UI updates optimistically
4. ✅ Turn internet back on
5. ✅ **Verify:** Like syncs to Firestore (or reverts if sync fails)

---

## 🔧 Technical Implementation

### **Data Structure in Firestore**

**Before (Old Posts):**
```json
{
  "title": "My Blog Post",
  "content": "...",
  "author": {...},
  "createdAt": "...",
  "imageUrl": "..."
}
```

**After (New Posts with Likes):**
```json
{
  "title": "My Blog Post",
  "content": "...",
  "author": {...},
  "createdAt": "...",
  "imageUrl": "...",
  "likedBy": ["userId1", "userId2", "userId3"]  // ✅ NEW
}
```

### **Optimistic Updates Pattern**

```kotlin
// 1. Update UI immediately
val updatedPost = post.copy(likedBy = newLikedByList)
_uiState.update { /* update with optimistic data */ }

// 2. Update Firestore in background
repository.toggleLike(...).onFailure {
    // 3. Revert if it fails
    _uiState.update { /* revert to original */ }
}
```

**Why Optimistic Updates?**
- ✅ Instant user feedback (feels fast)
- ✅ Better UX (no waiting for network)
- ✅ Graceful error handling (reverts on failure)

### **Like Count Calculation**

```kotlin
val likeCount: Int get() = likedBy.size
```

- Simple and efficient
- No separate counter field needed
- Always accurate (count = array length)

### **Checking if User Liked**

```kotlin
fun isLikedBy(userId: String): Boolean = likedBy.contains(userId)
```

- O(n) lookup (acceptable for typical like counts)
- Could be optimized with a Set if needed for very popular posts

---

## 🎓 What You Learned

1. **Optimistic UI Updates** - Update UI immediately, sync in background
2. **Array Fields in Firestore** - Store lists of data efficiently
3. **User-specific State** - Track which users performed actions
4. **Error Recovery** - Revert optimistic updates on failure
5. **Callback Patterns** - Pass actions up through composable hierarchy
6. **Stateful Icons** - Change icon appearance based on data

---

## 🔮 Future Enhancements (Optional)

- **Like Animation** - Add heart animation when liking
- **Double-tap to Like** - Like posts by double-tapping the image
- **Like Notifications** - Notify authors when someone likes their post
- **Who Liked List** - Show list of users who liked a post
- **Like History** - View all posts a user has liked
- **Unlike Confirmation** - Ask "Are you sure?" before unliking
- **Like Limit** - Prevent spam by rate-limiting likes
- **Trending Posts** - Sort by most liked posts

---

## 📊 Database Impact

### **Migration Notes**
- **Existing posts** will have `likedBy = []` (empty array) by default
- **No migration needed** - Firestore handles missing fields gracefully
- **New posts** will automatically include the `likedBy` field

### **Storage Considerations**
- Each like adds ~20 bytes (user ID string)
- 100 likes = ~2KB per post
- Acceptable for most use cases
- For viral posts (10,000+ likes), consider pagination or separate collection

---

**Status:** ✅ Feature Complete and Ready to Use!

**Bookmark Feature:** ❌ Successfully Removed
