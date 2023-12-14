let orderTableElement = jQuery("#order_table_body");
let totalPriceElement = jQuery("#total-price");
let totalPrice = 0;
function handleSalesData(data) {
    console.log(data);
    for (let i = 0; i < data.length; i++) {
        jQuery.ajax({
            dataType: "json",  // Setting return data type
            method: "GET",// Setting request method
            url: "api/single-movie?id=" + data[i]["movieId"],
            success: (movieRes) => {
                console.log("movieRes", movieRes);
                let rowHTML = "";
                rowHTML += "<tr>";
                rowHTML += "<th>" + data[i]["saleId"] + "</th>";
                rowHTML += "<th>" + movieRes["movie_title"] + "</th>";
                rowHTML += "<th>" + data[i]["quantity"] + "</th>";
                rowHTML += "<th>" + movieRes["movie_price"].toFixed(2) + "</th>"
                rowHTML += "</tr>";
                orderTableElement.append(rowHTML);
                totalPrice += movieRes["movie_price"]
                totalPriceElement.empty();
                totalPriceElement.append("Total Price: $" + totalPrice.toFixed(2));
            }
        });

    }
}

function handlePayment() {
    var today = new Date();
    var day = String(today.getDate()).padStart(2, '0');
    var month = String(today.getMonth() + 1).padStart(2, '0'); //January is 0!
    var year = today.getFullYear();

    var today = year + '-' + month + '-' + day;
    jQuery.ajax({
        type: "POST",
        url: "api/place-order",
        data: {saleDate: today},
        dataType: "json",
        success: (response) => {
            handleSalesData(response);
        },
        error: () => paymentErrorElement.append("Error occurred during transaction")
    });
}

handlePayment();