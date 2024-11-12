Sports News App

Requirements Implementation
- Uses Cronet (UrlRequest helper class) to retrieve web content
- Uses Gson to deserialize JSON responses
- Shows details of a randomly picked sports article about NBA
- Article has image URL (urlToImage attribute)
- Image is cached in app internal storage
- Article content is cached as text file in app internal storage
- Uses cached image and article text until expiration (10 seconds)
- Maintains timestamp of last API retrieval
- Uses System.currentTimeMillis() for timestamp checking

Individual Contribution
Sehoon Park
- Implemented all core functionalities:
  - Set up Cronet for network requests
  - Implemented Gson deserialization
  - Created caching system for articles and images
  - Added 10-second cache expiration logic
  - Implemented timestamp tracking
- Built user interface using Jetpack Compose:
  - Created article display layout
  - Added loading states
  - Implemented error handling
- Integrated NBA sports news API:
  - Added random article selection
  - Implemented image caching
  - Added content caching
- Handled all documentation and version control

Screenshot


<img width="217" alt="image" src="https://github.com/user-attachments/assets/1343223d-3ebe-46b7-a51c-188cf07aa27a"> loading from cache
<img width="205" alt="image" src="https://github.com/user-attachments/assets/74f55cef-fc7e-4d19-838f-3b17136ca8f3"> loading new data


