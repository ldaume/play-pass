/**
 * Created by leonard on 10.02.16.
 */
$(document).ready(function () {
    var table = $('#passwordTable').DataTable({
        "ajax": "/play-pass/data/pws",
        "columns": [
            {"data": "account"},
            {"data": "login"},
            {"data": "password"},
            {"data": "webSite"}/*,
             { "data": "notes" }*/
        ]
    });

    new $.fn.dataTable.Buttons(table, {
        buttons: [
            'copy', 'excel', 'pdf', 'print'
        ]
    });

    table.buttons().container()
        .insertBefore($('.col-sm-12', table.table().container()));
});
