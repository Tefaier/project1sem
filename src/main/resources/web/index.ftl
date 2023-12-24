<html lang="ru">
<head>
  <meta charset="UTF-8">
  <title>Booking Service</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/gh/yegor256/tacit@gh-pages/tacit-css-1.6.0.min.css"/>
  <script>
          function deleteBooking(value){
              window.location = "/room/unbook/" + value;
              window.location.reload();
          }
  </script>
</head>

<body>

<h1>Список бронирований по пользователю</h1>
<table>
  <tr>
    <th>Время начала</th>
    <th>Время конца</th>
    <th>Аудитория</th>
    <th></th>
  </tr>
    <#list bookings as booking>
      <tr>
        <td>${booking.timeFrom}</td>
        <td>${booking.timeTo}</td>
        <td>${booking.roomName}</td>
        <button onclick="deleteBooking(${booking.Id})">Удалить</button>
      </tr>
    </#list>
</table>

</body>

</html>
