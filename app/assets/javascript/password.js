/**
 * Created by leonard on 10.02.16.
 */
var chosenPassword = null;

function fillEditForm(d) {
    // `d` is the original data object for the row
    chosenPassword = d;
    $("#modalEditor #inputTitle").val(d.account);
    $("#modalEditor #inputLogin").val(d.login);
    $("#modalEditor #inputPassword").val(d.password);
    $("#modalEditor #inputUrl").val(d.webSite);
    $("#modalEditor #inputComment").val(d.comments);
}

(function ($) {
    $.isBlank = function (string) {
        return (!string || $.trim(string) === "");
    };
})(jQuery);

function hideModalEditError() {
    document.getElementById("modalEditorError").style.display = 'none';
}

function showModalEditError() {
    document.getElementById("modalEditorError").style.display = 'block';
}

function passwordFormToJSON() {
    return {
        account: $("#modalEditor #inputTitle").val(),
        login: $("#modalEditor #inputLogin").val(),
        password: $("#modalEditor #inputPassword").val(),
        webSite: $("#modalEditor #inputUrl").val(),
        comments: $("#modalEditor #inputComment").val()
    };
}

jQuery.extend({
    postJSON: function (params) {
        return jQuery.ajax(jQuery.extend(params, {
            type: "POST",
            data: JSON.stringify(params.data),
            dataType: "json",
            contentType: "application/json",
            processData: false
        }));
    }
});

function extractDomain(url) {
    var domain;
    //find & remove protocol (http, ftp, etc.) and get domain
    if (url.indexOf("://") > -1) {
        domain = url.split('/')[2];
    }
    else {
        domain = url.split('/')[0];
    }

    //find & remove port number
    domain = domain.split(':')[0];

    return domain;
}

// render table
$(document).ready(function () {
    var table = $('#passwordTable').DataTable({
        "ajax": "/play-pass/password/data/pws",
        "order": [[3, 'asc']],
        "columns": [
            {"data": "account"},
            {"data": "login"},
            {"data": "password", "render": function (data, type, full, meta) {
                return htmlEncode(data);
            }},
            {
                "data": "webSite", "render": function (data, type, full, meta) {
                if ($.isBlank(data) || /\s/.test(data)) {
                    return data;
                } else {
                    return "<a target='_blank' href='" + data + "'" + ">" + extractDomain(data) + "</a>";
                }
            }
            },
            {"data": "comments"},
            {
                "data": null,
                "defaultContent": "",
                "orderable": false,
                "width": "10px",
                "render": function (data, type, full, meta) {
                    return '<a class="editButton fa fa-pencil-square-o" style="text-decoration:none" data-toggle="modal" data-target="#modalEditor"></a>';
                }
            }
        ]
    });

    // buttons above the table
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

    // Add event listener for edit button
    $('#passwordTable tbody').on('click', 'td:last-child', function () {
        hideModalEditError();
        var tr = $(this).closest('tr');
        var row = table.row(tr);
        fillEditForm(row.data());
    });


    // change event for the editForm
    $("#modalEditor #changePasswordButton").on('click', function () {
        hideModalEditError();
        var oldPassword = chosenPassword;
        var newPassword = passwordFormToJSON();
        if (oldPassword.account !== newPassword.account || oldPassword.login !== newPassword.login || oldPassword.password !== newPassword.password || oldPassword.webSite !== newPassword.webSite || oldPassword.comments !== newPassword.comments) {
            $.postJSON({
                url: "/play-pass/password/edit",
                data: {from: oldPassword, to: newPassword},
                success: function (data) {
                    $("#modalEditor").modal("hide");
                    table.ajax.reload();
                },
                error: function () {
                    $("#modalEditor #modalEditorError").text("Could not change the password.");
                    showModalEditError();
                }
            });
            $("#modalEditor").modal("hide");
        } else {
            $("#modalEditor #modalEditorError").text("Nothing has changed.");
            showModalEditError();
        }
    });

    // delete event for the editForm
    $("#modalEditor #deletePasswordButton").on('click', function () {
        hideModalEditError();
        var oldPassword = chosenPassword;
        var newPassword = passwordFormToJSON();
        if (oldPassword.account !== newPassword.account || oldPassword.login !== newPassword.login || oldPassword.password !== newPassword.password || oldPassword.webSite !== newPassword.webSite || oldPassword.comments !== newPassword.comments) {
            $("#modalEditor #modalEditorError").text("Only unchanged passwords can be deleted.");
            showModalEditError();
        } else {
            hideModalEditError();
            if (confirm('Are you sure?')) {
                $.postJSON({
                    url: "/play-pass/password/delete",
                    data: oldPassword,
                    success: function (data) {
                        $("#modalEditor").modal("hide");
                        table.ajax.reload();
                    },
                    error: function () {
                        $("#modalEditor #modalEditorError").text("Could not delete the password.");
                        showModalEditError();
                    }
                });
            }
        }
    });
});
