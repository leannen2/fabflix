function handleSessionData(sessionData) {
    console.log("handling session data: ", sessionData);
    if (!sessionData["loggedIn"]) {
        console.log("user is not logged in... redirecting to login page");
        window.location.replace("login.html");
    }
}

export function checkSession() {
    jQuery.ajax({
        dataType: "json", // Setting return data type
        method: "GET", // Setting request method
        url: "api/session",
        success: (sessionData) => handleSessionData(sessionData),
    });
}