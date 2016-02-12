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
            {"data": "webSite"},
            {"data": "comments"}
        ]
    });


    new $.fn.dataTable.Buttons(table, {
        buttons: [
            {
                className: 'fa fa-plus', text: ' Add Password',
                action: function (e, dt, node, config) {
                    window.location.replace("/play-pass/password/add");
                }
            }
        ]
    });

    new $.fn.dataTable.Buttons(table, {
        buttons: [
            {extend: 'copy', className: 'fa fa-files-o', text: '', "sToolTip": 'Copy to clipBoard'},
            {extend: 'excel', className: 'fa fa-file-excel-o', text: ''},
            {extend: 'pdf', className: 'fa fa-file-pdf-o', text: ''},
            {extend: 'print', className: 'fa fa-print', text: ''}
        ]
    });


    table.buttons().container()
        .insertBefore($('.col-sm-12', table.table().container()));
});
