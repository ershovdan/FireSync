from django.urls import path
from . import views


app_name = "interface"

urlpatterns = [
    path('', views.home),
    path('add/', views.add),
    path('list/', views.list),
    path('list/more_info/', views.more_info),
    path('operations_get_data/', views.getData),
    path('preferences', views.preferences),
]
