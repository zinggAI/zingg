import streamlit as st
import os

st.set_page_config(page_title="Zingg", layout="wide")

APP_DIR = os.path.dirname(os.path.abspath(__file__))
LOGO_PATH = os.path.join(APP_DIR, "assets", "zingg_logo_full.png")

st.image(LOGO_PATH, width=380)

st.markdown("""
Zingg is an end to end pipeline for entity resolution. Most data about
people, companies, or products is scattered across systems and never quite
agrees with itself. Zingg resolves it into one accurate record, running
natively on your own data, with no data leaving your environment.

Common uses:
""")

col1, col2 = st.columns(2)
with col1:
    st.markdown("""
**Agentic AI & Knowledge Graphs**
Give AI agents and RAG systems a stable, resolved identity to reason about, so they act on one accurate record, not scattered duplicates.

**Customer & Supplier 360**
One trusted record per customer or supplier across every source system.

**Master Data Management**
Unify customer, supplier, and product domains into a single mastered view.
""")
with col2:
    st.markdown("""
**KYC & Compliance / Fraud & AML**
Resolve onboarding data, KYC documents, and watchlists into one profile; surface connected parties hidden across separate records.

**Composable CDP**
Build a unified customer profile directly on your own warehouse or lakehouse.

**GDPR & CCPA**
Find every record tied to a person to answer data subject access and deletion requests.
""")

st.markdown("---")

st.page_link("pages/1_Get_Started.py", label="Get started with entity resolution", icon="➡️")

st.markdown("---")

col1, col2, col3 = st.columns(3)
with col1:
    st.markdown("**📖 Documentation**  \n[docs.zingg.ai](https://docs.zingg.ai)")
with col2:
    st.markdown("**💬 Community**  \n[Join the Zingg Slack](https://join.slack.com/t/zinggai/shared_invite/zt-w7zlcnol-vEuqU9m~Q56kLLUVxRgpOA)")
with col3:
    st.markdown("**🏢 Zingg Enterprise**  \nNeed persistent Zingg IDs or incremental matching to keep agents and AI systems working on a live, up-to-date entity graph? [Contact us](mailto:sonal@zingg.ai)")