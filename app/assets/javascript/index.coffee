$ ->
  $(document).ready(() ->
#$('ul.nav > li a').click((e) ->
#  e.preventDefault();
#  $('ul.nav > li').removeClass('active')
#  $(this).closest('li').addClass('active'))

# datatables
    $('#passwordTable').DataTable({
      "ajax": "/play-pass/data/pws",
      "columns": [
        {"data": "account"},
        {"data": "login"},
        {"data": "password"},
        {"data": "webSite"}#,
#        { "data": "notes" }
      ]
    })
  )
