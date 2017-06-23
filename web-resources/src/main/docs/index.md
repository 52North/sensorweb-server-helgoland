---
layout: default
---

# Series REST API
{: .page-header }

<div>
  {% for section in site.sections %}
    <section>
      {% if section.title %}
      <h2 id="{{ page.title | downcase | replace: ' ', '-' }}">{{section.title}}</h2>
      {% else %}
      <h2 class="page-header">{{ section.title }}</h2>
      {% endif %}
      
      {{ section.content | markdownify }}
    </section>
  {% endfor %}
</div>

