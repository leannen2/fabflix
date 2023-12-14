let errorElement = jQuery("#login_error");
function handleResponse(response) {
    console.log("response", response)
    if (response["login_auth"] === 'valid') {
        window.location.replace("index.html");
    } else {
        errorElement.empty();
        errorElement.append("Login information invalid")
    }


}
jQuery("form").submit(function (event) {
    jQuery.ajax({
        type: "POST",
        // TODO: create login servlet for employees, create employees table
        url: "api/login",
        data: $('form').serialize(),
        dataType: "json",
        success: (response) => handleResponse(response),
        error: (response) => {
            errorElement.empty();
            console.log("login error", response);
            errorElement.append(response["responseJSON"]["errorMessage"]);
        }
    }).done(function (data) {
        console.log(data);
    });

    event.preventDefault();
});