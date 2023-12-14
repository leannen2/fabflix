import csv
import sys

file1 = sys.argv[1]

ts = 0
tj = 0
count = 0

fileObj1 = open(file1)

for line in fileObj1.readlines():
    nums = line.split(" ")
    tsNum, tjNum = int(nums[0]), int(nums[1])
    
    ts += tsNum / 1000000
    tj += tjNum / 1000000
    count += 1

fileObj1.close()

if len(sys.argv) > 2:
    file2 = sys.argv[2]
    fileObj2 = open(file2)
    for line in fileObj2.readlines():
        nums = line.split(" ")
        tsNum, tjNum = int(nums[0]), int(nums[1])
        
        ts += tsNum / 1000000
        tj += tjNum / 1000000
        count += 1

    fileObj2.close()


ts /= count
tj /= count

print(f'average ts: {ts} ms')
print(f'average tj: {tj} ms')