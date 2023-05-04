from django.urls import path
from . import views


app_name = "files"

urlpatterns = [
    path('get_json/', views.getJsonFile),
    path('get_file/', views.getFile),
]
