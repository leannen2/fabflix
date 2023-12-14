function getAndInsertData(tableName) {
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/metadata?table=" + tableName,
        success: (resultData) => insertIntoTable(tableName, resultData)
    });
}

function insertIntoTable(tableName, data) {
    console.log(tableName, data);
    console.log("#" + tableName + "_table_body");
    let element = jQuery("#" + tableName + "_table_body");
    let rowHTML = ""
    for (let i = 0; i < data.length; i++) {
        rowHTML += "<tr> " +
            "<th>" + data[i]["attribute"] + "</th>" +
            "<th>" + data[i]["type"] + "</th>" +
            "</tr>";
    }
    console.log('rowHTML ', rowHTML)
    element.append(rowHTML);
}

getAndInsertData("movies");
getAndInsertData("stars");
getAndInsertData("stars_in_movies");
getAndInsertData("genres");
getAndInsertData("genres_in_movies");
getAndInsertData("customers");
getAndInsertData("sales");
getAndInsertData("creditcards");
getAndInsertData("ratings");