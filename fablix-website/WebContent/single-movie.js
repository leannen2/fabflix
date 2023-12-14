import { checkSession } from './session.js';

checkSession();

let itemMessageContainer = jQuery("#item-message-container");
let itemMessageElement = jQuery("#item-message");

function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating single movie elements");
    let movieTitleElement = jQuery("#movie_info"); // TODO: change element
    console.log("result data", resultData)
    movieTitleElement.append("Movie Title: " + resultData["movie_title"] + " (" + resultData["movie_year"] + ")");
    movieTitleElement.append("<p>Directed By: " + resultData["movie_director"] + "</p>");

    console.log("Populating genres body with: ", resultData["movie_genres"]);
    let genresElement = jQuery("#genres_body");
    for (let i = 0; i < resultData["movie_genres"].length; i++) {
        genresElement.append('<a href="movie-list.html?genre=' + resultData["movie_genres"][i] + '&pageNumber=1">' + resultData["movie_genres"][i] + "</a><br>");
    }

    console.log("Populating movie stars with: ", resultData["movie_stars"]);
    let starsHTML = "";
    for (let i = 0; i < resultData["movie_stars"].length; i++) {
        starsHTML += "<p>"
            +'<a href="single-star.html?id=' + resultData["movie_stars"][i]["star_id"] + '">'
            + resultData["movie_stars"][i]["star_name"]
            + "</a>"
            + "</p>";
    }
    let movieStarsElement = jQuery("#stars_body");
    movieStarsElement.append(starsHTML);

    let movieRatingElement = jQuery("#movie_rating_body")
    movieRatingElement.append(resultData["movie_rating"] + "/10");

}

let movieId = getParameterByName('id');
console.log("running single movie js");
// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movieId, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});

function handleAddToCartClick() {
    console.log(movieId, "is being added to cart");
    jQuery.ajax({
        type: "POST",
        url: "api/shopping-cart/add-item",
        data: {item: movieId},
        dataType: "json",
        success: (response) => {
            itemMessageElement.empty();
            itemMessageElement.append("Movie added to cart");
            console.log("item added to cart ", response)
            itemMessageContainer.fadeIn(function() {
                itemMessageContainer.fadeOut();
            });
        },
        error: () => {
            itemMessageElement.empty();
            itemMessageElement.append("Movie was not able to be added to cart");
            itemMessageContainer.fadeIn(function() {
                itemMessageContainer.fadeOut();
            });
        }
    });
}

function handleGoToMovieListClick() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/session",
        success: (response) => {
            window.location.href = response["currentLink"];
        }
    });
}

jQuery("#add_btn").click(handleAddToCartClick);
jQuery("#go-to-movie-list").click(handleGoToMovieListClick);