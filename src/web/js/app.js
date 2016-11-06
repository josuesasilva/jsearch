$(document).ready(function () {
    console.log("ready!");

    /*
     * States
     */

    var endPointBase = "http://localhost:4567";
    var endPointSearch = endPointBase + "/search?query=";
    var endPointIndexer = endPointBase + "/performIndexer";
    var endPointLog = endPointBase + "/log";
    var endPointAutoComplete = endPointBase + "/autocomplete?query=";

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

    $("#user-search-input").submit(function (event) {
        var query = $('#search-input').val();
        if (!(query.trim() === '')) {
            showSearchResult(query);
        }
        event.preventDefault();
    });

    $("#user-input-top").submit(function (event) {
        var query = $('#search-input-top').val();
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

        loadData(query);
    }

    function addItem(id, title, content) {
        $("#results").append("<a data-doc=" + id +
                " class='list-group-item doc-item'><h4 class='list-group-item-heading' data-doc="
                + id + ">" + title +
                "</h4><p class='list-group-item-text' data-doc= " + id + ">"
                + content + "</p></a>");
    }

    function loadData(query) {
        $.get(endPointSearch + query, function (data) {
            data = JSON.parse(data);
            console.log(data);

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
                    }
                });
            }
        });

    }

});