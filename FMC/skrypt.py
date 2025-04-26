import json
import numpy as np
import pandas as pd
import rasterio
from rasterio.transform import from_origin

# Wczytaj JSON z listą punktów
with open('heatmap2.json', 'r') as f:
    data = json.load(f)

# Zamień na DataFrame
df = pd.DataFrame(data)

# Podgląd danych
print("Przykładowe dane:")
print(df.head())

# Posortuj dane dla poprawnego układu siatki
df = df.sort_values(by=['lat', 'lon'], ascending=[False, True])

# Przygotowanie siatki
lats = sorted(df['lat'].unique(), reverse=True)  # Od góry (północy)
lons = sorted(df['lon'].unique())                # Od lewej (zachodu)

nrows = len(lats)
ncols = len(lons)
cellsize_lat = round(abs(lats[0] - lats[1]), 10) if nrows > 1 else 0.0001
cellsize_lon = round(abs(lons[1] - lons[0]), 10) if ncols > 1 else 0.0001
cellsize = min(cellsize_lat, cellsize_lon)

# Lewy górny róg (dla transformacji w rasterio)
x_origin = min(lons)
y_origin = max(lats)

# Stworzenie pustej macierzy
grid = np.full((nrows, ncols), -9999.0)  # NODATA_value

# Mapowanie wartości do siatki
lat_idx = {lat: i for i, lat in enumerate(lats)}
lon_idx = {lon: i for i, lon in enumerate(lons)}

for _, row in df.iterrows():
    i = lat_idx[row['lat']]
    j = lon_idx[row['lon']]
    grid[i, j] = row['value']

    # Definicja transformacji i metadanych
    x_origin_corrected = x_origin - (cellsize / 2)
    y_origin_corrected = y_origin + (cellsize / 2)
    transform = from_origin(x_origin_corrected, y_origin_corrected, cellsize, cellsize)

with rasterio.open(
    'heatmap.tif', 'w',
    driver='GTiff',
    height=nrows,
    width=ncols,
    count=1,
    dtype=grid.dtype,
    crs='EPSG:4326',  # WGS84
    transform=transform,
    nodata=-9999.0
) as dst:
    dst.write(grid, 1)

print("Gotowe! Zapisano raster GeoTIFF do pliku 'heatmap.tif'")
