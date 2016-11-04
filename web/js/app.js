$(document).ready(function() {
	console.log( "ready!" );

	/*
	 * States
	 */

	var endPointBase = "http://localhost:3000";
	var endPointSearch = endPointBase + "/search?query=";

	var availableTags = [
		"ActionScript",
		"AppleScript",
		"Asp",
		"BASIC",
		"C",
		"C++",
		"Clojure",
		"COBOL",
		"ColdFusion",
		"Erlang",
		"Fortran",
		"Groovy",
		"Haskell",
		"Java",
		"JavaScript",
		"Lisp",
		"Perl",
		"PHP",
		"Python",
		"Ruby",
		"Scala",
		"Scheme"
			];

	/* 	var lastXhr;
			$("#search-input").autocomplete({
			source: function( request, response ) {
			if (lastXhr) lastXhr.abort();
			lastXhr = $.getJSON(endPointSearch, request, function( data, status, xhr ) {
			if (xhr === lastXhr) {
			response(data);
			}
			});
			}
			}); */


	/*
	 * Default states
	 */

	defaultState();

	/*
	 * Events
	 */

	$("#search-input").autocomplete({
		source: availableTags
	});

	$("#request-user-input").click(function() {
		$("#simulation-input").hide();
		$("#user-search-input").show();	
	});

	$("#request-simulation").click(function() {
		$("#user-search-input").hide();	
		$("#simulation-input").show();
	});

	$("#user-search-input").submit(function(event) {
		showSearchResult();
		event.preventDefault();
	});

	$("#user-input-top").submit(function(event) {
		showSearchResult();
		event.preventDefault();
	});


	/*
	 * private methods
	 */

	function showLoading() {
		$("#search-input").blur(); 
		$('body').loading({
			stoppable: false,
			message: 'Processando...'
		});
	}

	function hideLoading() {
		$('body').loading('stop');
	}

	function defaultState() {
		$("#user-input-top").hide();
		$("#simulation-input").hide();
		$("#search-content-result").hide();
		$("#search-content-input").show();
		$("#search-content-input").focus();
	}

	function showSearchResult() {
		$("#search-content-input").hide();
		$("#user-search-input").hide();
		$("#user-input-top").show();
		$("#search-content-result").show();
		$("#menu-options").hide();

		loadData();
	}

	function addItem(title, content) {
		$("#results").append("<a href='#' class='list-group-item'><h4 class='list-group-item-heading'>" + title + 
				"</h4><p class='list-group-item-text'>" + content +"</p></a>");					
	}

	function loadData() {
		for (var i = 0; i < 10; i++) {
			addItem("teste", "Testando");
		}

		$("#result-size").text(10);
		$("#result-time").text("0,35 segundos");
	}

});