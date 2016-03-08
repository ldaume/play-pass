function htmlEncode(value){
    "use strict";
    //create a in-memory div, set it's inner text(which jQuery automatically encodes)
    //then grab the encoded contents back out.  The div never exists on the page.
    return $('<div/>').text(value).html();
}

function htmlDecode(value){
    "use strict";
    return $('<div/>').html(value).text();
}
