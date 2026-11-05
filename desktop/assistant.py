import socket
import tkinter as tk
from tkinter import ttk, messagebox

HOST, PORT = "127.0.0.1", 28741

def send(command):
    try:
        with socket.create_connection((HOST, PORT), timeout=2) as sock:
            sock.sendall((command + "\n").encode())
            return sock.recv(128).decode(errors="replace").strip()
    except OSError:
        messagebox.showerror("Starcatcher Assistant", "Не удалось связаться с модом. Убедитесь, что Minecraft запущен и мод установлен.")
        return None

root = tk.Tk()
root.title("Starcatcher Assistant")
root.geometry("380x330")
root.resizable(False, False)
style = ttk.Style(root)
style.theme_use("clam")

frame = ttk.Frame(root, padding=16)
frame.pack(fill="both", expand=True)
ttk.Label(frame, text="STAR CATCHER", font=("Segoe UI", 17, "bold")).pack(pady=(0, 12))
ttk.Label(frame, text="Автоматизация мини-игры и добычи наживки").pack(pady=(0, 14))

row = ttk.Frame(frame)
row.pack(fill="x")
ttk.Button(row, text="▶ Рыбалка", command=lambda: send("FISH_START")).pack(side="left", expand=True, fill="x", padx=3)
ttk.Button(row, text="▶ Наживка", command=lambda: send("BAIT_START")).pack(side="left", expand=True, fill="x", padx=3)

row2 = ttk.Frame(frame)
row2.pack(fill="x", pady=8)
ttk.Button(row2, text="Пауза", command=lambda: send("PAUSE")).pack(side="left", expand=True, fill="x", padx=3)
ttk.Button(row2, text="Продолжить", command=lambda: send("RESUME")).pack(side="left", expand=True, fill="x", padx=3)
ttk.Button(row2, text="Стоп", command=lambda: send("STOP")).pack(side="left", expand=True, fill="x", padx=3)

row3 = ttk.Frame(frame)
row3.pack(fill="x", pady=(0, 8))
ttk.Button(row3, text="Автопродажа COMMON: ВКЛ", command=lambda: send("SELL_COMMON_START")).pack(side="left", expand=True, fill="x", padx=3)
ttk.Button(row3, text="ВЫКЛ", command=lambda: send("SELL_COMMON_STOP")).pack(side="left", expand=True, fill="x", padx=3)

settings = ttk.LabelFrame(frame, text="Наживка: интервалы в тиках (20 тиков = 1 сек.)", padding=10)
settings.pack(fill="x", pady=8)
hold = tk.StringVar(value="35")
pause = tk.StringVar(value="10")
line = ttk.Frame(settings)
line.pack(fill="x")
ttk.Label(line, text="Удерживать ПКМ:").pack(side="left")
ttk.Entry(line, textvariable=hold, width=7).pack(side="right")
line = ttk.Frame(settings)
line.pack(fill="x", pady=(6, 0))
ttk.Label(line, text="Пауза:").pack(side="left")
ttk.Entry(line, textvariable=pause, width=7).pack(side="right")

def apply():
    try:
        h, p = int(hold.get()), int(pause.get())
        if not 1 <= h <= 1200 or not 0 <= p <= 1200:
            raise ValueError()
    except ValueError:
        messagebox.showwarning("Настройки", "Введите целые значения от 0 до 1200; удержание должно быть не меньше 1.")
        return
    send(f"SET_HOLD {h}")
    send(f"SET_PAUSE {p}")

ttk.Button(settings, text="Применить интервалы", command=apply).pack(fill="x", pady=(8, 0))
ttk.Label(frame, text="Сначала запустите Minecraft с модом Starcatcher Assistant.", wraplength=340).pack(pady=(8, 0))
root.mainloop()
