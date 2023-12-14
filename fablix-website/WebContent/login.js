let errorElement = jQuery("#login_error");
function handleResponse(response) {
    console.log("response", response)
    if (response["login_auth"] === 'valid') {
        window.location.replace("main-page.html");
    } else {
        errorElement.empty();
        errorElement.append("Login information invalid")
    }


}
jQuery("form").submit(function (event) {
    // var formData = {
    //     emailAddress: jQuery("#emailAddress").val(),
    //     password: jQuery("#password").val(),
    // };
    //
    // console.log($('form').serialize());

    jQuery.ajax({
        type: "POST",
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