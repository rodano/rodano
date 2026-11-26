shinyjs.setTokenToLocalStorage = function(params){
  window.localStorage.setItem("token", params);
}
  
  
$(document).on('shiny:connected', function(event) {
    // Retrieve the authentication token from localStorage
    var token = window.localStorage.getItem("token");
    // If the authentication token is null, do nothing
    if (token == null) {
        window.localStorage.setItem("token","")
    }
    // Otherwise, set the authentication token in the Shiny input
    Shiny.setInputValue("token", token);
});
