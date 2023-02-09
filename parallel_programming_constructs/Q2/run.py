# importing the required module
import matplotlib.pyplot as plt

# x axis values
x = [1,2,3,4,5,6,7,8,9,10,11,12]
# corresponding y axis values
y = [69.0991381052, 41.9201990946, 29.119227384, 25.3106521288, 23.0961657208, 19.552013724200002, 18.846306764799998, 17.1539236448, 15.7348642268, 15.6332107856, 16.0172351482, 16.468612959799998]

# plotting the points
plt.plot(x, y)

# naming the x axis
plt.xlabel('Number of Cores')
# naming the y axis
plt.ylabel('Time Taken')

# giving a title to my graph
plt.title('Cores v/s Time Graph')

# function to show the plot
plt.show()