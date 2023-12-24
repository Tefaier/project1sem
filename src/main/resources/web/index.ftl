<html lang="ru">
<head>
  <meta charset="UTF-8">
  <title>Booking Service</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/gh/yegor256/tacit@gh-pages/tacit-css-1.6.0.min.css"/>
  <script>
          async function deleteBooking(value){
              const response = await fetch("/room/unbook/" + value, {
              method: "DELETE"
              });
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
        <td><button onclick="deleteBooking(${booking.id})">Удалить</button></td>
      </tr>
    </#list>
</table>

</body>

</html>
