# macOS Setup for Zingg

This guide provides specific instructions for setting up the Zingg development environment on macOS.

### **Step 0: Initial OS Setup**

Homebrew is the preferred package manager for macOS and makes installing system dependencies much easier. While you can install dependencies manually, this guide uses Homebrew for simplicity.

Install Homebrew:
```bash
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
```
**Note for Apple Silicon:** If `brew` is not found, run:
`echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zshrc && source ~/.zshrc`

---

### **Step 1: Clone The Zingg Repository**

```bash
brew install git
```

---

### **Step 2: Install JDK 11 (Java Development Kit)**

* Install OpenJDK 11: `brew install openjdk@11`
* Link Java 11:
  * **Apple Silicon:** `sudo ln -sfn /opt/homebrew/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`
  * **Intel:** `sudo ln -sfn /usr/local/opt/openjdk@11/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-11.jdk`

---

### **Step 4: Install Apache Maven**

```bash
brew install maven
```

---

### **Step 5: Update Environment Variables (~/.zshrc)**

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-11.jdk/Contents/Home
export SPARK_HOME=/opt/spark
export SPARK_MASTER=local[*]
export ZINGG_HOME=<path_to_zingg>/assembly/target
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin:$JAVA_HOME/bin
```

---

### **Step 7: If you have any issue with 'SPARK_LOCAL_IP'**

1. **Find your IP:** `ipconfig getifaddr en0`
2. **Find your Hostname:** `hostname`
3. **Update hosts:** `sudo nano /etc/hosts` and add `[your-ip] [your-hostname]` at the bottom.

---

### **Next Steps**

After completing the macOS specific setup, return to the [main setup guide](./settingUpZingg.md) to complete the common steps (Step 3, Step 6, and Steps 8-11).
