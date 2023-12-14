let movieFormElement = jQuery("#add-movie-form");
let movieMessageElement = jQuery("#movie-message-container");
let starFormElement = jQuery("#add-star-form");
let starMessageElement = jQuery("#star-message-container");

function handleResponse(response) {
    console.log(response);
    movieMessageElement.empty();
    movieMessageElement.append(response["message"]);
}

function handleStarResponse(response) {
    console.log(response);
    starMessageElement.empty();
    starMessageElement.append(response["message"]);

}
movieFormElement.submit(function (event) {
    jQuery.ajax({
        type: "POST",
        url: "api/add-movie",
        data: movieFormElement.serialize(),
        dataType: "json",
        success: (response) => handleResponse(response),
        error: (response) => {
            movieMessageElement.empty();
            console.log("add movie error", response);
        }
    });

    event.preventDefault();
});

starFormElement.submit(function (event) {
    jQuery.ajax({
        type: "POST",
        url: "api/add-star",
        data: starFormElement.serialize(),
        dataType: "json",
        success: (response) => handleStarResponse(response),
        error: (response) => {
            starMessageElement.empty();
            console.log("add star error", response);
        }
    });

    event.preventDefault();
});