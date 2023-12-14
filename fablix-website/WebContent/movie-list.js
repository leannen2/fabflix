import { checkSession } from './session.js';

checkSession();

let numMoviesElement = jQuery("#num_of_movies_displayed");
let itemMessageContainer = jQuery("#item-message-container");
let itemMessageElement = jQuery("#item-message");
let noNext = false;

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
function handleMovieResults(resultData) {
    console.log("handleMovieResults: populating movies table from resultData", resultData);

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();
    if (resultData.length < numMoviesElement.val()) {
        noNext = true;
    }
    // Iterate through resultData, no more than 10 entries
    for (let i = 0; i < resultData.length; i++) {
        let rowHTML = "";
        rowHTML += "<tr>";
        rowHTML += "<th>"
            + '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">'
            + resultData[i]["movie_title"] + "</a>"
            + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_year"] + "</th>";
        rowHTML += "<th>" + resultData[i]["movie_director"] + "</th>";
        let genresString = resultData[i]["movie_genres"].join(", ");
        rowHTML += "<th>" + genresString + "</th>";
        rowHTML += "<th>";
        for (let j = 0; j < resultData[i]["movie_stars"].length; j++) {
            rowHTML += '<a href="single-star.html?id=' + resultData[i]["movie_stars"][j]["star_id"] + '">' + resultData[i]["movie_stars"][j]["star_name"] + "</a>";
            if (j < resultData[i]["movie_stars"].length-1) {
                rowHTML += ", ";
            }
        }
        rowHTML += "</th>"
        rowHTML += "<th>" + resultData[i]["rating"] + "</th>"
        rowHTML += "<th>" +
            '<button type="button" class="btn btn-outline-primary" id="' + resultData[i]["movie_id"] + '">Add to Shopping Cart</button>' +
            "</th>";
        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
}

let genre = getParameterByName('genre');
let title = getParameterByName('title');
let year = getParameterByName('year');
let starName = getParameterByName('starName');
let director = getParameterByName('director');
let alpha = getParameterByName('alpha');
let pageNumber = getParameterByName('pageNumber');
// let order1 = getParameterByName('order1') ?? "title asc";
// let order2 = getParameterByName('order2') ?? "rating desc";
let order1 = "rating desc";
let order2 = "title asc";
console.log(genre, title, year, starName, director, alpha);
function constructServletLink() {
    let servletLink = "api/movie-list";
    var params = false;
    if (genre != null) {
        servletLink += "?genre=" + genre;
        params = true;
    }
    if (title != null) {
        servletLink += (params) ? "&" : "?";
        servletLink += "title=" + title;
        params = true;
    }
    if (year != null) {
        servletLink += (params) ? "&" : "?";
        servletLink += "year=" + year;
        params = true;
    }
    if (director != null) {
        servletLink += (params) ? "&" : "?";
        servletLink += "director=" + director;
        params = true;
    }
    if (alpha != null) {
        servletLink += (params) ? "&" : "?";
        servletLink += "alpha=" + alpha;
        params = true;
    }
    if (starName != null) {
        servletLink += (params) ? "&" : "?";
        servletLink += "starName=" + starName;
        params = true;
    }
    servletLink += (params) ? "&" : "?";
    servletLink += "pageNumber=" + pageNumber + "&numMoviesDisplay=" + numMoviesElement.val();
    servletLink += "&order1=" + order1 + "&order2=" + order2;
    return servletLink
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
function displayMovieList() {
    //console.log(numMoviesElement.val());

    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: constructServletLink(), // Setting request url, which is mapped by StarsServlet in Stars.java
        success: (resultData) => handleMovieResults(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
    });
}

function handleNextClick() {
    if (noNext) {
        return;
    }
    var pageInt = parseInt(pageNumber, 10);
    pageInt += 1;
    var pageIndex = window.location.href.search("pageNumber=");
    var nextPage = window.location.href.substring(0, pageIndex) + "pageNumber=" + pageInt;
    console.log("next page: ", nextPage)
    window.location.href = nextPage;
}

function handlePrevClick() {
    var pageInt = parseInt(pageNumber, 10);
    if (pageInt == 1)
        return
    pageInt -= 1;
    var pageIndex = window.location.href.search("pageNumber=");
    var nextPage = window.location.href.substring(0, pageIndex) + "pageNumber=" + pageInt + "&numMoviesDisplay=" + numMoviesElement.val();
    console.log("next page: ", nextPage)
    window.location.href = nextPage;
}

function updateSortInSessionData() {
    var sortData = {
        order1: order1,
        order2: order2,
        currentLink: window.location.href,
        currPagination: numMoviesElement.val()
    };

    console.log("sort data sent: ", sortData);

    jQuery.ajax({
        type: "POST",
        url: "api/session",
        data: sortData,
        dataType: "json",
        success: (response) => {
            console.log("sort updated ", response);
            displayMovieList();
        }
    });
}

function handleTitleDescClick() {
    if (!order1.includes("title")) {
        order2 = order1;
    }
    order1 = "title desc";
    updateSortInSessionData();
    //displayMovieList();
}

function handleTitleAscClick() {
    if (!order1.includes("title")) {
        order2 = order1;
    }
    order1 = "title asc";
    updateSortInSessionData();
    displayMovieList();
}

function handleRatingAscClick() {
    if (!order1.includes("rating")) {
        order2 = order1;
    }
    order1 = "rating asc";
    updateSortInSessionData();
    displayMovieList();
}

function handleRatingDescClick() {
    if (!order1.includes("rating")) {
        order2 = order1;
    }
    order1 = "rating desc";
    updateSortInSessionData();
    displayMovieList();
}

function assignOrderBy(sessionData) {
    console.log("assigning order: ", sessionData);
    console.log(sessionData["movieListParams"]["order1"], sessionData["movieListParams"]["order2"]);
    order1 = sessionData["movieListParams"]["order1"];
    order2 = sessionData["movieListParams"]["order2"];
    numMoviesElement.val(sessionData["currPagination"]);
    updateSortInSessionData();
    console.log(order1, order2);
    console.log(constructServletLink());
    displayMovieList();
}

function handleAddToCartClick() {
    console.log($(this).attr('id'), "is being added to cart");
    jQuery.ajax({
        type: "POST",
        url: "api/shopping-cart/add-item",
        data: {item: $(this).attr('id')},
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

jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/session",
    success: (resultData) => assignOrderBy(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});

jQuery("#next_button").click(handleNextClick);
jQuery("#prev_button").click(handlePrevClick);
jQuery("#title_asc").click(handleTitleAscClick);
jQuery("#title_desc").click(handleTitleDescClick);
jQuery("#rating_asc").click(handleRatingAscClick);
jQuery("#rating_desc").click(handleRatingDescClick);
jQuery("#movie_table").on( 'click', 'button', handleAddToCartClick);
numMoviesElement.change(() => {
    updateSortInSessionData();
    displayMovieList();
});
