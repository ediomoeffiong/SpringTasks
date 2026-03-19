import os
import glob

files = glob.glob(r'c:\Users\Ediomo Emma Effiong\Documents\FifthLab\Projects\SpringTasks\src\main\resources\templates\**\*.html', recursive=True)

for f in files:
    with open(f, 'r', encoding='utf-8') as file:
        content = file.read()
    
    new_content = content.replace('text-white', 'text-primary')
    new_content = new_content.replace('style="color: #94a3b8 !important;"', '')
    new_content = new_content.replace('opacity-50', '')
    new_content = new_content.replace('opacity-75', '')
    new_content = new_content.replace('style="background: rgba(255, 255, 255, 0.05);"', 'class="bg-overlay-dark"')
    
    # Restore specific button contrasts that were strictly white on colored backgrounds
    new_content = new_content.replace('btn-primary text-primary', 'btn-primary text-white')
    new_content = new_content.replace('bg-primary text-primary', 'bg-primary text-white')
    new_content = new_content.replace('btn-primary fw-bold text-primary', 'btn-primary fw-bold text-white')
    new_content = new_content.replace('bg-success text-primary', 'bg-success text-white')
    new_content = new_content.replace('bg-danger text-primary', 'bg-danger text-white')
    
    if content != new_content:
        with open(f, 'w', encoding='utf-8') as file:
            file.write(new_content)
        print(f"Updated {f}")
print("done")
