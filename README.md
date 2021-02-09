Affirm Android technical challenge


For this excercise I spent about a little over an hour working. I decided to use MVVM architecture because it is suggested for Android development and allows for easier testing. Another benefit of this architectural decision is that it separates the view from any of the application logic. The only additional library that I decided to use was kotlin coroutines because of the ease of use when offloading threads for networking calls. 

The most obvious imporvement for this app would be the user experience and the basic ui which both would need a lot of work before releasing to real customers. Another improvement would be how the Restaurant, YelpRestaurant and ZomatoRestaurant data classes are all completely unrelated. To improve on speed and space efficiency we could make the Restaurant data class into a regular class and use inheritance for the other two to prevent parseing objects on each API call. Another area for improvement would be to signify when all Yelp or all Zomato restaurant have been seen rather than just looping the resaurants. 
