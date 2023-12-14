import { checkSession } from './session.js';

checkSession();

let shoppingCartTableElement = jQuery("#shopping_cart_table_body");
let totalPriceElement = jQuery("#total-price");

function handleShoppingCart(shoppingCart) {
    let totalPrice = 0;
    shoppingCartTableElement.empty();
    console.log("filling shopping cart table with: ", shoppingCart["shoppingObject"]);
    let cart = shoppingCart["shoppingObject"];
    for (let key in cart) {
        jQuery.ajax({
            dataType: "json",  // Setting return data type
            method: "GET",// Setting request method
            url: "api/single-movie?id=" + key,
            success: (movieRes) => {
                console.log(key, movieRes);
                let rowHTML = "";
                // console.log("inside ajax", key + " -> " + cart[key]);
                rowHTML += "<tr>";
                rowHTML += "<th>" + movieRes["movie_title"] + "</th>";
                rowHTML += "<th>" + cart[key] + "</th>";
                rowHTML += "<th> " +
                    "<button class='btn btn-danger decrease_quant_btn' id='remove_" + movieRes["movie_id"] + "'>-</button> " +
                    "<button class='btn btn-primary increase_quant_btn' id='add_" + movieRes["movie_id"] + "'>+</button>" +
                    "</th>";
                rowHTML += "<th>" + movieRes["movie_price"].toFixed(2) + "</th>";
                rowHTML += "<th>" +
                    "<button class ='btn btn-outline-danger remove-all-btn' id='remove-all-" + movieRes["movie_id"] + "'>Remove from Cart</button>" +
                    "</th>";
                rowHTML += "</tr>";
                shoppingCartTableElement.append(rowHTML);
                totalPrice += cart[key] * movieRes["movie_price"];
                console.log("total price= ", totalPrice);
                totalPriceElement.empty();
                totalPriceElement.append("Total Price: $" + totalPrice.toFixed(2));
            }
        });
    }
    // console.log("total price after ", totalPrice);
    // totalPriceElement.append(totalPrice);
}

function addToTotalPrice(price) {

}

function displayShoppingCart() {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/shopping-cart",
        success: handleShoppingCart
    });
}

function handleDecreaseQuantClick() {
    let movieId = $(this).attr('id').substring(7);
    // console.log(movieId, "is being removed from cart");
    jQuery.ajax({
        type: "POST",
        url: "api/shopping-cart/remove-item",
        data: {item: movieId},
        dataType: "json",
        success: (response) => {
            console.log("item removed from cart ", response)
            displayShoppingCart();
        }
    });
}

function handleIncreaseQuantClick() {
    let movieId = $(this).attr('id').substring(4);
    // console.log(movieId, "is being added to cart");
    jQuery.ajax({
        type: "POST",
        url: "api/shopping-cart/add-item",
        data: {item: movieId},
        dataType: "json",
        success: (response) => {
            console.log("item added to cart ", response)
            displayShoppingCart();
        }
    });
}

function handleRemoveItemClick() {
    let movieId = $(this).attr('id').substring(11);
    // console.log(movieId, "is being removed from cart");
    jQuery.ajax({
        type: "POST",
        url: "api/shopping-cart/remove-item",
        data: {
            item: movieId,
            removeAll: "true"
        },
        dataType: "json",
        success: (response) => {
            console.log("all of item removed from cart ", response)
            displayShoppingCart();
        }
    });
}

function handleGoToMovieListClick() {
    window.location.href = "movie-list.html?pageNumber=1";
}

displayShoppingCart();

jQuery("#shopping_cart_table").on( 'click', '.decrease_quant_btn', handleDecreaseQuantClick);
jQuery("#shopping_cart_table").on( 'click', '.increase_quant_btn', handleIncreaseQuantClick);
jQuery("#shopping_cart_table").on('click', '.remove-all-btn', handleRemoveItemClick);
jQuery("#movie-list-btn").click(handleGoToMovieListClick);