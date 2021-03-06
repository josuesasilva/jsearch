$(document).ready(function () {
	console.log("ready!");

	/*
	 * States
	 */

	var endPointBase = "http://localhost:4567";
	var endPointSearch = endPointBase + "/search?query=";
	var endPointIndexer = endPointBase + "/performIndexer";
	var endPointLog = endPointBase + "/log";
	var endPointDocument = endPointBase + "/document?doc=";
	var endPointAutoComplete = endPointBase + "/autocomplete?query=";
	var page = 1;
	var query = "";

	/*
	 * Default states
	 */

	defaultState();

	/*
	 * Events
	 */

	var lastXhr;

	$("#search-input").autocomplete({
		source: function (request, response) {
			var query = $('#search-input').val();

			if (query !== undefined && query !== '') {

				if (lastXhr) {
					lastXhr.abort();
				}

				lastXhr = $.getJSON(endPointAutoComplete + query, request, function (data, status, xhr) {
					if (xhr === lastXhr) {
						console.log(data);
						response(data.suggestions);
					}
				});

			}
		}
	});

	$("#request-user-input").click(function () {
		$("#simulation-input").hide();
		$("#user-search-input").show();
	});

	$("#request-simulation").click(function () {
		$("#user-search-input").hide();
		$("#simulation-input").show();
	});

	$("#request-indexer").click(function () {
		showLoading('Indexando documentos...');
		$.get(endPointIndexer)
			.done(function () {
				hideLoading();
			})
		.fail(function () {
			hideLoading();
			$('body').loading({
				stoppable: true,
				message: 'Ocorreu um erro ao indexar os documentos.'
			});
		});
	});

	$("#btn-next").click(function() {
		if (page < 5) {
			page++;
		}
		console.log(page);
		loadData(query, page);
	});

	$("#btn-prev").click(function() {
		if (page > 2) {
			page--;
		}
		console.log(page);
		loadData(query, page);
	});

	$("#btn-1").click(function() {
		page = 1;
		console.log(page);
		loadData(query, 1);
	});

	$("#btn-2").click(function() {
		page = 2;
		console.log(page);
		loadData(query, 2);
	});

	$("#btn-3").click(function() {
		page = 3;
		console.log(page);
		loadData(query, 3);
	});

	$("#btn-4").click(function() {
		page = 4;
		console.log(page);
		loadData(query, 4);
	});

	$("#btn-5").click(function() {
		page = 5;
		console.log(page);
		loadData(query, 5);
	});

	$("#user-search-input").submit(function (event) {
		query = $('#search-input').val();
		if (!(query.trim() === '')) {
			showSearchResult(query);
		}
		event.preventDefault();
	});

	$("#user-input-top").submit(function (event) {
		query = $('#search-input-top').val();
		if (!(query.trim() === '')) {
			showSearchResult(query);
		}
		event.preventDefault();
	});

	/*
	 * private methods
	 */

	function showLoading(msg) {
		$("#search-input").blur();
		$('body').loading({
			stoppable: false,
			message: msg
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

	function showSearchResult(query) {
		$("#search-content-input").hide();
		$("#user-search-input").hide();
		$("#user-input-top").show();
		$("#search-content-result").show();
		$("#menu-options").hide();

		loadData(query, 1);
	}

	function summarize(string) {
		var count = 150;
		var result = "";
		for (i = 0; i < string.length; i++) {
			if (count > 0) {
				result+=string[i];
				count--;
			}
		}
		return result+="...";
	}

	function addItem(id, title, content) {
		$("#results").append("<a data-doc=" + id +
				" class='list-group-item doc-item'><h4 class='list-group-item-heading' data-doc="
				+ id + ">" + title +
				"</h4><p class='list-group-item-text' data-doc= " + id + ">"
				+ summarize(content) + "</p></a>");
	}

	function loadData(query, page) {
		$("#results").empty();

		showLoading("Carregando...");

		$.get(endPointSearch + query + "&page=" + page, function (data) {
			data = JSON.parse(data);
			console.log(data);

			hideLoading();

			if (data.docs !== undefined) {
				data.docs.forEach(function (value) {
					addItem(value.id, value.title, value.content);
				});

				$("#result-size").text(data.docs.length);
				$("#result-time").text(data.time / 1000.0 + " segundos");
				$("#search-input-top").val(query);

				$(".doc-item").click(function (event) {
					var target = $(event.target);
					var id = target.attr('data-doc');

					if (!(id === undefined)) {
						var url = endPointLog + "?doc=" + id + "&query=" + query;
						$.post(url, function (data) {
							console.log(data);
						});

						window.location.replace(endPointDocument+id);
					}
				});
			}
		});

	}

});
