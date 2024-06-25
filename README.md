<div>
<h3>1. Image-Resizer</h3>
<b>개발하게 된 이유?</b><br>
: 회사에서 수동으로 이미지를 리사이징하여 업로드 해야하는 작업이 있었는데 너무 번거롭게 느껴져서 만들어보게 되었다.<br><br>
<b> 작동 방식 (24.06.25)</b><br>
: Swagger에서 이미지를 업로드하면 메모리상에서 이미지 정보를 얻어와 리사이징 후 로컬 폴더에 저장<br><br>
<b> 어려웠던 부분 </b><br>
: JPEG 파일 타입의 경우 안드로이드 폰으로 촬영시 너비*높이가 리사이징하면 바뀌어서 나오던가, 사진이 눕혀져서 나오는 상황이 발생<br>
-> ExifDirectory로 orientation을 구해와 이미지 회전각(?)을 확인하고 코드를 작성하는 과정이 어렵게 느껴졌음<br><br>
<b> 추후 계획</b><br>
: S3에 업로드가 되어있는 사진들이 불러와서 이미지 정보를 조회하고, 조건에 벗어나는 경우(너비 1200 기준) 리사이징되어 S3 경로에 업로드 되는 방법으로 구현해보기
  
</div>