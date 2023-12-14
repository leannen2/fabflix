import { checkSession } from './session.js';

checkSession();

let paymentErrorElement = jQuery("#payment-error-message");

function handleAuthPaymentResponse(response) {
    paymentErrorElement.empty();
    if (response["payment_auth"] === "failed") {
        paymentErrorElement.append("Invalid payment information");
    } else {
        window.location.href = "order-confirmation.html";
    }

}

jQuery("#payment-form").submit(function(event) {
    jQuery.ajax({
        type: "POST",
        url: "api/auth-payment",
        data: $("#payment-form").serialize(),
        dataType: "json",
        success: (response) => handleAuthPaymentResponse(response)
    }).done(function (data) {
        console.log("auth payment response: ", data);
    });

    event.preventDefault();
});