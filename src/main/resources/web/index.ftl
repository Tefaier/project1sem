<html lang="ru">
<head>
  <meta charset="UTF-8">
  <title>Booking Service</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/gh/yegor256/tacit@gh-pages/tacit-css-1.6.0.min.css"/>
</head>

<body>

<h1>Список бронирований по пользователю</h1>
<table>
  <tr>
    <th>ID брони</th>
    <th>Время начала</th>
    <th>Время конца</th>
    <th>Аудитория</th>
  </tr>
    <#list bookings as booking>
      <tr>
        <td>${booking.id}</td>
        <td>${booking.timeFrom}</td>
        <td>${booking.timeTo}</td>
        <td>${booking.roomName}</td>
      </tr>
    </#list>
</table>

</body>

</html>
