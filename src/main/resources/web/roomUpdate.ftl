<html lang="ru">
<head>
  <meta charset="UTF-8">
  <title>Обновить комнату</title>
  <link rel="stylesheet"
        href="https://cdn.jsdelivr.net/gh/yegor256/tacit@gh-pages/tacit-css-1.6.0.min.css"/>
  <script>
        async function updateRoom(){
              let name = document.querySelector(".roomName").value;
              let from = document.querySelector(".roomFrom").value;
              let to = document.querySelector(".roomTo").value;
              let noCheck = !document.querySelector(".noCheck").checked;

              const jsonData = {
                name: name,
                from: from,
                to: to,
                noCheck: noCheck
              }

              let responseField = document.querySelector(".response");

              const response = await fetch(window.location.href, {
                method: "POST",
                body: JSON.stringify(jsonData),
                headers: {
                'Content-type': 'application/json; charset=UTF-8'
                }
              });
              responseField.innerHTML = await response.text();
              responseField.style.visibility = "visible";
        }
    </script>
</head>

<body>

<h1>Обновление комнаты</h1>
<label>Название комнаты</label>
<input class="roomName" type="text" value=${roomName}><br>
<label>Время, с которого открыта</label>
<input class="roomFrom" type="time" <#if timeFrom??> value=${timeFrom} </#if>><br>
<label>Время, до которого открыта</label>
<input class="roomTo" type="time" <#if timeTo??> value=${timeTo} </#if>><br>
<label>Ограничивать ли время</label>
<input class="noCheck" type="checkbox" <#if !noCheck> checked </#if>><br>
<button onclick="updateRoom()">Обновить</button><br>
<p class="response" style="visibility: hidden"></p>

</body>

</html>