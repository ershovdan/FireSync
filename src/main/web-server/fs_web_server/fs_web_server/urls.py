from django.contrib import admin
from django.urls import path, include

import files.urls, interface.urls

urlpatterns = [
    path('', include(interface.urls, namespace="interface")),
    path('files/', include(files.urls, namespace="files")),
]
