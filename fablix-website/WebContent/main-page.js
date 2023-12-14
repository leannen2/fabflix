let browseLetterElement = jQuery("#browse_letter");
let letters = ["A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"];

for (let i = 0; i < 26; i++) {
    browseLetterElement.append('<a href="movie-list.html?alpha='
        + letters[i] + '&pageNumber=1">' + letters[i]
        + "</a> ");
}
browseLetterElement.append('<a href="movie-list.html?alpha=*&pageNumber=1">*</a>');

let genresElement = jQuery("#browse_genre");

function handleGenres(genres) {
    for (let i = 0; i < genres.length; i++) {
        genresElement.append('<a href="movie-list.html?genre=' + genres[i] + '&pageNumber=1">' + genres[i] + "</a><br>");
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/genres",
    success: (genres) => handleGenres(genres)
});


jQuery("#search_button").click(function (event) {
    let title = jQuery("#search_title").val();
    let year = jQuery("#search_year").val();
    let director = jQuery("#search_director").val();
    let star = jQuery("#search_star").val();
    let params = false;
    let servletLink = "movie-list.html"
    if (title != "") {
        servletLink += "?title=" + title;
        params = true;
    }
    if (year != "") {
        servletLink += (params) ? "&" : "?";
        servletLink += "year=" + year;
        params = true;
    }
    if (director != "") {
        servletLink += (params) ? "&" : "?";
        servletLink += "director=" + director;
    }
    if (star != "") {
        servletLink += (params) ? "&" : "?";
        servletLink += "starName=" + star;
    }

    servletLink += "&pageNumber=1";

    console.log("search clicked, redirecting to " + servletLink);
    window.location.href = servletLink;
});

